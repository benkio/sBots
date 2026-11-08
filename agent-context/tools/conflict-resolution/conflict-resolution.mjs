#!/usr/bin/env node
/**
 * Conflict-resolution helper for `AddData` changes.
 *
 * It intentionally only uses Node built-ins so it can run without installing
 * dependencies. It writes JSON in the same circe `spaces2` shape used by the
 * bot data files, so untouched files stay byte-identical.
 *
 * See `README.md` in this folder for usage.
 */

import fs from "node:fs";
import path from "node:path";
import { execFileSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const TYPE_ORDER = ["GifFile", "MarkdownFile", "Mp3File", "VideoFile", "PhotoFile", "Sticker", "Document"];

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main();
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  if (args.help || args._.includes("help")) {
    printUsage();
    return;
  }

  const repoRoot = findRepoRoot(args.repo ?? process.cwd());
  const state = loadState(args, repoRoot);

  const wantsAnalyze = args.analyze || (!args.validate && !args["dedupe-list"] && !args["merge-plan"]);
  if (wantsAnalyze) analyze(state, args);
  if (args["dedupe-list"]) dedupeList(state, args);
  if (args["merge-plan"]) applyPlan(state, args);
  if (args.validate) validate(state, args);

  if (args.write) {
    if (state.dirtyList) writeJson(state.paths.list, state.list, state.listRaw);
    if (state.dirtyReplies) writeJson(state.paths.replies, state.replies, state.repliesRaw);
    if (state.dirtyList || state.dirtyReplies) {
      console.log("\nFiles written.");
    } else {
      console.log("\nNo changes to write.");
    }
  } else if (state.dirtyList || state.dirtyReplies) {
    console.log("\nDRY RUN: no files written. Re-run with --write to apply.");
  }
}

// ---------------------------------------------------------------------------
// CLI
// ---------------------------------------------------------------------------

function printUsage() {
  console.log(`Usage:
  node conflict-resolution.mjs --bot <botId> [options]
  node conflict-resolution.mjs --list <path> --replies <path> [options]

Options:
  --analyze              Inventory: placeholders, duplicates, new/unused media,
                         and candidate existing bundles (default action).
  --validate             Check list duplicates, reply/list consistency, and
                         whether AddData media is referenced.
  --dedupe-list          Remove duplicate list entries. Keeps the last
                         occurrence (the AddData one) by default.
  --keep-first           With --dedupe-list, keep the first occurrence instead.
  --merge-plan <path>    Apply a reviewed merge plan (see README).
  --write                Actually write files. Without it every action is a
                         dry run.
  --strict               Treat validation warnings as errors.
  --force                Allow removing a placeholder that contains media
                         from bases not covered by the merge plan.
  --repo <path>          Repository root (default: auto-detect from cwd).
  --help                 Show this message.

Merge plan:
  {
    "merges": [
      { "base": "2Orecchie1Bocca2", "match": "sentire il doppio" },
      { "base": "Nodi", "match": "ci siamo sciolti", "removePlaceholder": true }
    ]
  }

  "match" is a case-insensitive substring of a trigger value (or of the raw
  regex body). It must select exactly one existing non-placeholder bundle.
`);
}

function parseArgs(argv) {
  const out = { _: [] };
  const booleans = new Set([
    "write",
    "analyze",
    "validate",
    "dedupe-list",
    "keep-first",
    "strict",
    "force",
    "help",
  ]);

  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    if (!arg.startsWith("--")) {
      out._.push(arg);
      continue;
    }
    const key = arg.slice(2);
    if (booleans.has(key)) {
      out[key] = true;
    } else {
      const value = argv[++i];
      if (value === undefined) throw new Error(`Missing value for --${key}`);
      out[key] = value;
    }
  }
  return out;
}

// ---------------------------------------------------------------------------
// Loading
// ---------------------------------------------------------------------------

function findRepoRoot(start) {
  let dir = path.resolve(start);
  for (;;) {
    if (fs.existsSync(path.join(dir, ".git"))) return dir;
    const parent = path.dirname(dir);
    if (parent === dir) return path.resolve(start);
    dir = parent;
  }
}

function resolvePaths(args, repoRoot) {
  if (args.list && args.replies) {
    return {
      list: path.resolve(repoRoot, args.list),
      replies: path.resolve(repoRoot, args.replies),
    };
  }

  if (!args.bot) {
    throw new Error("Provide --bot <botId> or both --list and --replies.");
  }

  const botsRoot = path.join(repoRoot, "modules", "bots");
  const matches = fs
    .readdirSync(botsRoot, { withFileTypes: true })
    .filter(entry => entry.isDirectory())
    .map(entry => path.join(botsRoot, entry.name))
    .filter(dir => fs.existsSync(path.join(dir, `${args.bot}_list.json`)));

  if (matches.length !== 1) {
    throw new Error(
      `Could not uniquely resolve bot '${args.bot}'. Matches: ${matches.join(", ") || "none"}`
    );
  }

  const botDir = matches[0];
  return {
    list: path.join(botDir, `${args.bot}_list.json`),
    replies: path.join(botDir, "src", "main", "resources", `${args.bot}_replies.json`),
  };
}

function loadState(args, repoRoot) {
  const paths = resolvePaths(args, repoRoot);
  const listRaw = fs.readFileSync(paths.list, "utf8");
  const repliesRaw = fs.readFileSync(paths.replies, "utf8");
  const list = JSON.parse(listRaw);
  const replies = JSON.parse(repliesRaw);
  const botId = args.bot ?? path.basename(paths.list).replace(/_list\.json$/, "");

  return {
    repoRoot,
    botId,
    paths,
    listRaw,
    repliesRaw,
    list,
    replies,
    headList: readHeadJson(repoRoot, paths.list),
    dirtyList: false,
    dirtyReplies: false,
    warnings: [],
    errors: [],
  };
}

function readHeadJson(repoRoot, file) {
  try {
    const rel = path.relative(repoRoot, file);
    const content = execFileSync("git", ["show", `HEAD:${rel}`], {
      cwd: repoRoot,
      maxBuffer: 1024 * 1024 * 512,
      encoding: "utf8",
      stdio: ["ignore", "pipe", "ignore"],
    });
    return JSON.parse(content);
  } catch {
    return null;
  }
}

// ---------------------------------------------------------------------------
// JSON model helpers
// ---------------------------------------------------------------------------

function triggersOf(bundle) {
  const textTrigger = bundle?.trigger?.TextTrigger;
  if (!textTrigger) return [];
  return (textTrigger.triggers ?? []).flatMap(value => {
    if (value.StringTextTriggerValue !== undefined) return [value.StringTextTriggerValue];
    if (value.RegexTextTriggerValue !== undefined) return [value.RegexTextTriggerValue.trigger];
    return [];
  });
}

function isPlaceholder(bundle) {
  return triggersOf(bundle).includes("new data");
}

function mediaOf(bundle) {
  const mediaFiles = bundle?.reply?.MediaReply?.mediaFiles;
  if (!Array.isArray(mediaFiles)) return [];
  return mediaFiles.map(media => Object.values(media)[0]?.filepath).filter(Boolean);
}

function mediaTypeOf(filename) {
  if (filename.endsWith("Gif.mp4")) return "GifFile";
  if (filename.endsWith(".mp3")) return "Mp3File";
  if (filename.endsWith(".mp4")) return "VideoFile";
  if (filename.endsWith(".md")) return "MarkdownFile";
  if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")) return "PhotoFile";
  if (filename.endsWith(".sticker")) return "Sticker";
  return "Document";
}

function baseOf(filename, botId) {
  const prefix = botId ? new RegExp(`^${escapeRegExp(botId)}_`) : null;
  const name = prefix ? filename.replace(prefix, "") : filename;
  for (const suffix of ["Gif.mp4", ".sticker", ".mp3", ".mp4", ".md", ".jpeg", ".jpg", ".png"]) {
    if (name.endsWith(suffix)) return name.slice(0, -suffix.length);
  }
  const dot = name.lastIndexOf(".");
  return dot === -1 ? name : name.slice(0, dot);
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function mediaTypeRank(filename) {
  return TYPE_ORDER.indexOf(mediaTypeOf(filename));
}

function makeMedia(filename) {
  return { [mediaTypeOf(filename)]: { filepath: filename, replyToMessage: false } };
}

function unique(values) {
  return [...new Set(values)];
}

function shortList(values, limit = 6) {
  const list = [...values];
  if (list.length <= limit) return list.join(", ");
  return `${list.slice(0, limit).join(", ")} ... (+${list.length - limit})`;
}

// ---------------------------------------------------------------------------
// Actions
// ---------------------------------------------------------------------------

function analyze(state, args) {
  section(`Analyze ${state.botId}`);
  console.log(`list:    ${rel(state.repoRoot, state.paths.list)}`);
  console.log(`replies: ${rel(state.repoRoot, state.paths.replies)}`);

  const names = state.list.map(entry => entry.filename);
  const duplicates = duplicateValues(names);
  const references = new Set(state.replies.flatMap(mediaOf));
  const headNames = state.headList ? new Set(state.headList.map(entry => entry.filename)) : null;
  const newNames = headNames ? unique(names.filter(name => !headNames.has(name))) : [];
  const unusedNew = newNames.filter(name => !references.has(name));
  const missingFromList = [...references].filter(name => !names.includes(name));
  const placeholders = state.replies.filter(isPlaceholder);

  console.log(`\nlist entries:        ${names.length}`);
  console.log(`reply bundles:       ${state.replies.length}`);
  console.log(`placeholders:        ${placeholders.length}`);
  console.log(`duplicate filenames: ${duplicates.length}`);

  if (duplicates.length > 0) {
    console.log(`  DUPLICATES: ${duplicates.join(", ")}`);
  }

  if (headNames) {
    console.log(`new list media:      ${newNames.length}`);
    console.log(`new media unused:    ${unusedNew.length}`);
    if (unusedNew.length > 0) {
      console.log(`  UNUSED: ${unusedNew.join(", ")}`);
    }
  } else {
    console.log("new list media:      n/a (HEAD version unavailable)");
  }

  if (missingFromList.length > 0) {
    console.log(`  REFERENCES MISSING FROM LIST: ${missingFromList.join(", ")}`);
  }

  for (const placeholder of placeholders) {
    const placeholderMedia = mediaOf(placeholder);
    const bases = unique(placeholderMedia.map(filename => baseOf(filename, state.botId)));
    const mediaSet = new Set(placeholderMedia);
    const candidates = state.replies
      .filter(bundle => !isPlaceholder(bundle))
      .filter(bundle => {
        const bundleMedia = mediaOf(bundle);
        return bundleMedia.some(filename => bases.includes(baseOf(filename, state.botId)));
      });

    console.log(`\n  placeholder bases: ${bases.join(", ")}`);
    console.log(`    media: ${placeholderMedia.join(", ")}`);

    if (candidates.length === 0) {
      console.log("    candidates: none — likely genuinely new content");
      continue;
    }

    for (const candidate of candidates) {
      const missing = mediaOf(candidate).filter(filename => mediaSet.has(filename));
      console.log(`    candidate: [${triggersOf(candidate).join(" | ")}]`);
      console.log(
        `      media: ${mediaOf(candidate).length} file(s); same-base reference(s): ${
          missing.join(", ") || "(none directly, base match only)"
        }`
      );
    }
  }
}

function dedupeList(state, args) {
  section(`Deduplicate list ${state.botId}`);

  const keepFirst = Boolean(args["keep-first"]);
  const indices = new Map();
  state.list.forEach((entry, index) => {
    if (!indices.has(entry.filename)) indices.set(entry.filename, []);
    indices.get(entry.filename).push(index);
  });

  const remove = new Set();
  for (const [filename, positions] of indices) {
    if (positions.length < 2) continue;
    const keep = keepFirst ? positions[0] : positions[positions.length - 1];
    for (const position of positions) {
      if (position !== keep) remove.add(position);
    }
    console.log(
      `${filename}: ${positions.length} entries, keeping ${
        keepFirst ? "first" : "last (AddData)"
      } at index ${keep}`
    );
  }

  if (remove.size === 0) {
    console.log("No duplicate entries found.");
    return;
  }

  state.list = state.list.filter((_, index) => !remove.has(index));
  state.dirtyList = true;
  console.log(`Removed ${remove.size} duplicate entr${remove.size === 1 ? "y" : "ies"}.`);
}

function applyPlan(state, args) {
  section(`Apply merge plan ${state.botId}`);
  const planPath = path.resolve(state.repoRoot, args["merge-plan"]);
  const plan = JSON.parse(fs.readFileSync(planPath, "utf8"));
  const merges = plan.merges;

  if (!Array.isArray(merges) || merges.length === 0) {
    throw new Error(`Plan ${planPath} must contain a non-empty "merges" array.`);
  }

  const placeholdersToRemove = new Set();
  const handledBases = new Set(merges.map(merge => merge.base));

  for (const merge of merges) {
    if (!merge.base || !merge.match) {
      throw new Error(`Invalid merge entry: ${JSON.stringify(merge)}`);
    }

    const matchingPlaceholders = state.replies.filter(
      bundle =>
        isPlaceholder(bundle) &&
        mediaOf(bundle).some(filename => baseOf(filename, state.botId) === merge.base)
    );

    if (matchingPlaceholders.length === 0) {
      throw new Error(`No placeholder found for base '${merge.base}'.`);
    }

    for (const placeholder of matchingPlaceholders) {
      const bases = unique(mediaOf(placeholder).map(filename => baseOf(filename, state.botId)));
      const outside = bases.filter(base => !handledBases.has(base));
      if (outside.length > 0 && !args.force) {
        throw new Error(
          `Placeholder for '${merge.base}' also contains ${outside.join(", ")}. ` +
            "Add those bases to the plan or use --force."
        );
      }
    }

    const targetCandidates = state.replies.filter(
      bundle =>
        !isPlaceholder(bundle) &&
        triggersOf(bundle).some(trigger =>
          trigger.toLowerCase().includes(merge.match.toLowerCase())
        )
    );

    if (targetCandidates.length !== 1) {
      const details = targetCandidates.map(bundle => `[${triggersOf(bundle).join(" | ")}]`).join("; ");
      throw new Error(
        `Match '${merge.match}' for base '${merge.base}' selected ${targetCandidates.length} bundles. ` +
          `Expected exactly 1.${details ? ` Candidates: ${details}` : ""}`
      );
    }

    const target = targetCandidates[0];
    const existing = new Set(mediaOf(target));
    const placeholderMedia = unique(matchingPlaceholders.flatMap(mediaOf));
    const toAdd = placeholderMedia
      .filter(filename => !existing.has(filename))
      .sort((a, b) => mediaTypeRank(a) - mediaTypeRank(b) || a.localeCompare(b));

    console.log(`\n${merge.base} -> [${triggersOf(target).join(" | ")}]`);
    console.log(`  adding: ${toAdd.join(", ") || "(none — removing placeholder only)"}`);

    for (const filename of toAdd) {
      target.reply.MediaReply.mediaFiles.push(makeMedia(filename));
    }

    if (merge.removePlaceholder !== false) {
      for (const placeholder of matchingPlaceholders) placeholdersToRemove.add(placeholder);
    }
  }

  if (placeholdersToRemove.size > 0) {
    state.replies = state.replies.filter(bundle => !placeholdersToRemove.has(bundle));
    state.dirtyReplies = true;
    console.log(`\nRemoved ${placeholdersToRemove.size} placeholder bundle(s).`);
  }
}

function validate(state, args) {
  section(`Validate ${state.botId}`);

  const names = state.list.map(entry => entry.filename);
  const duplicates = duplicateValues(names);
  const references = unique(state.replies.flatMap(mediaOf));
  const missingFromList = references.filter(name => !names.includes(name));
  const headNames = state.headList ? new Set(state.headList.map(entry => entry.filename)) : null;
  const newNames = headNames ? unique(names.filter(name => !headNames.has(name))) : [];
  const unusedNew = newNames.filter(name => !references.includes(name));
  const placeholders = state.replies.filter(isPlaceholder);

  if (duplicates.length > 0) {
    state.errors.push(`duplicate list filenames: ${duplicates.join(", ")}`);
  }
  if (missingFromList.length > 0) {
    state.errors.push(`reply media missing from list: ${missingFromList.join(", ")}`);
  }
  if (unusedNew.length > 0) {
    state.warnings.push(`new AddData media not referenced by any reply: ${unusedNew.join(", ")}`);
  }

  console.log(`list entries:   ${names.length}`);
  console.log(`reply bundles:  ${state.replies.length}`);
  console.log(`placeholders:   ${placeholders.length}`);
  console.log(`duplicates:     ${duplicates.length}`);
  if (headNames) console.log(`new media:      ${newNames.length} (unused: ${unusedNew.length})`);

  for (const placeholder of placeholders) {
    const bases = unique(mediaOf(placeholder).map(filename => baseOf(filename, state.botId)));
    const candidates = state.replies.filter(
      bundle =>
        !isPlaceholder(bundle) &&
        mediaOf(bundle).some(filename => bases.includes(baseOf(filename, state.botId)))
    );
    const label = bases.join(", ");
    if (candidates.length > 0) {
      console.log(`  kept placeholder '${label}' still has same-base bundle(s); review manually.`);
    } else {
      console.log(`  kept placeholder '${label}' looks genuinely new.`);
    }
  }

  if (state.warnings.length > 0) {
    console.log("\nWarnings:");
    for (const warning of state.warnings) console.log(`  - ${warning}`);
  }

  if (state.errors.length > 0) {
    console.log("\nErrors:");
    for (const error of state.errors) console.log(`  - ${error}`);
    process.exitCode = 1;
    return;
  }

  if (args.strict && state.warnings.length > 0) {
    console.log("\nStrict mode: warnings treated as errors.");
    process.exitCode = 1;
    return;
  }

  console.log("\nValidation passed.");
}

// ---------------------------------------------------------------------------
// Output
// ---------------------------------------------------------------------------

function section(title) {
  console.log(`\n=== ${title} ===`);
}

function rel(root, file) {
  return path.relative(root, file);
}

function duplicateValues(values) {
  const seen = new Set();
  const duplicates = new Set();
  for (const value of values) {
    if (seen.has(value)) duplicates.add(value);
    else seen.add(value);
  }
  return [...duplicates];
}

// ---------------------------------------------------------------------------
// Serialization (circe spaces2-compatible)
// ---------------------------------------------------------------------------

function serialize(value, indent = 0) {
  const pad = "  ".repeat(indent + 1);
  const closePad = "  ".repeat(indent);

  if (Array.isArray(value)) {
    if (value.length === 0) return `[\n${closePad}]`;
    return `[\n${value.map(item => pad + serialize(item, indent + 1)).join(",\n")}\n${closePad}]`;
  }

  if (value !== null && typeof value === "object") {
    const keys = Object.keys(value);
    if (keys.length === 0) return `{\n${pad}\n${closePad}}`;
    return `{\n${keys
      .map(key => `${pad}${JSON.stringify(key)} : ${serialize(value[key], indent + 1)}`)
      .join(",\n")}\n${closePad}}`;
  }

  return JSON.stringify(value);
}

function writeJson(file, value, original) {
  const trailingNewline = original.endsWith("\n") ? "\n" : "";
  fs.writeFileSync(file, serialize(value) + trailingNewline, "utf8");
}

export { serialize, baseOf, mediaTypeOf, mediaOf, triggersOf };
