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

  const actionRequested =
    args.validate ||
    args["dedupe-list"] ||
    args["merge-plan"] ||
    args["check-inputtest"] ||
    args.suggest ||
    args.resolve ||
    args.changes ||
    args.report;
  const wantsAnalyze = args.analyze || !actionRequested;
  if (wantsAnalyze) analyze(state, args);
  if (args.changes) showChanges(state, args);
  if (args.suggest) suggest(state, args);
  if (args.resolve) resolveAll(state, args);
  if (args["dedupe-list"]) dedupeList(state, args);
  if (args["merge-plan"]) applyPlan(state, args);
  if (args["check-inputtest"]) checkInputTest(state, args);
  if (args.validate) validate(state, args);
  if (args.report) writeReport(state, args);

  if (args.write) {
    if (state.dirtyList) writeJson(state.paths.list, state.list, state.listRaw);
    if (state.dirtyReplies) writeJson(state.paths.replies, state.replies, state.repliesRaw);
    if (state.dirtyInputTest) writeLines(state.inputTestPath, state.inputTestLines, state.inputTestRaw);
    if (state.dirtyList || state.dirtyReplies || state.dirtyInputTest) {
      console.log("\nFiles written.");
    } else {
      console.log("\nNo changes to write.");
    }
  } else if (state.dirtyList || state.dirtyReplies || state.dirtyInputTest) {
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
  --changes              Show list/replies changes vs HEAD with triggers and
                         added media (no ad-hoc diff scripts needed).
  --suggest              Classify placeholders and print a ready-to-review
                         merge plan; ambiguous bases are left for a human.
  --check-inputtest      Check inputTest.txt expectations against the current
                         reply bundles using the bot's exact matching logic.
  --resolve              One-shot: dedupe list + apply suggested merges +
                         patch inputTest.txt (additions only). Dry-run unless
                         --write is given.
  --validate             Check list duplicates, reply/list consistency,
                         AddData media references and inputTest.txt.
  --report <path>        Write a filled report from report-template.md.
  --dedupe-list          Remove duplicate list entries. Keeps the last
                         occurrence (the AddData one) by default.
  --keep-first           With --dedupe-list, keep the first occurrence instead.
  --merge-plan <path>    Apply a reviewed merge plan (see README).
  --write                Actually write files. Without it every action is a
                         dry run.
  --strict               Treat validation warnings as errors.
  --force                Allow removing a placeholder that contains media
                         from bases not covered by the merge plan.
  --no-inputtest         Skip inputTest.txt in --validate.
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
    "check-inputtest",
    "suggest",
    "resolve",
    "changes",
    "no-inputtest",
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
    headReplies: readHeadJson(repoRoot, paths.replies),
    inputTestPath: path.join(path.dirname(paths.list), "src", "test", "resources", "inputTest.txt"),
    dirtyList: false,
    dirtyReplies: false,
    dirtyInputTest: false,
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

function applyPlan(state, args, planOverride) {
  section(`Apply merge plan ${state.botId}`);
  const planPath = planOverride ? null : path.resolve(state.repoRoot, args["merge-plan"]);
  const plan = planOverride ?? JSON.parse(fs.readFileSync(planPath, "utf8"));
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

// ---------------------------------------------------------------------------
// Fast analysis helpers (replace ad-hoc scripts in the task workflow)
// ---------------------------------------------------------------------------

function showChanges(state, args) {
  section(`Changes vs HEAD ${state.botId}`);
  const headNames = state.headList ? new Set(state.headList.map(e => e.filename)) : null;
  const names = state.list.map(e => e.filename);
  if (headNames) {
    const added = unique(names.filter(n => !headNames.has(n)));
    const replaced = names.filter(n => headNames.has(n)).filter(n => {
      const old = state.headList.find(e => e.filename === n);
      const cur = state.list.find(e => e.filename === n);
      return JSON.stringify(old) !== JSON.stringify(cur);
    });
    console.log(`new list files (${added.length}): ${added.join(", ") || "-"}`);
    console.log(`replaced list entries (${replaced.length}): ${replaced.join(", ") || "-"}`);
  }
  console.log("\nbundles with media changes vs HEAD:");
  let count = 0;
  for (let i = 0; i < state.replies.length; i++) {
    const before = state.headReplies ? mediaOf(state.headReplies[i]) : [];
    const after = mediaOf(state.replies[i]);
    const added = after.filter(f => !before.includes(f));
    if (added.length === 0) continue;
    count++;
    console.log(`  #${i} [${triggersOf(state.replies[i]).join(" | ")}]`);
    console.log(`    + ${added.join(", ")}`);
  }
  console.log(`  total changed bundles: ${count}`);
}

function uniqueMatchFor(bundle, state) {
  for (const trigger of triggersOf(bundle)) {
    const probe = trigger.toLowerCase();
    const matches = state.replies.filter(
      b => !isPlaceholder(b) && triggersOf(b).some(t => t.toLowerCase().includes(probe))
    );
    if (matches.length === 1 && matches[0] === bundle) return probe;
  }
  return null;
}

function classifyPlaceholders(state) {
  return state.replies.filter(isPlaceholder).map(placeholder => {
    const media = mediaOf(placeholder);
    const bases = unique(media.map(f => baseOf(f, state.botId)));
    const perBase = bases.map(base => {
      const candidates = state.replies.filter(
        b => !isPlaceholder(b) && mediaOf(b).some(f => baseOf(f, state.botId) === base)
      );
      return { base, candidates, match: candidates.length === 1 ? uniqueMatchFor(candidates[0], state) : null };
    });
    const auto = perBase.every(p => p.candidates.length === 1 && p.match);
    const kind = perBase.every(p => p.candidates.length === 0)
      ? "genuinely new"
      : auto ? "replacement/completion (auto)" : "replacement/completion (needs human choice)";
    return { placeholder, media, perBase, kind, auto };
  });
}

function suggestedMerges(state) {
  const merges = [];
  const skipped = [];
  for (const item of classifyPlaceholders(state)) {
    if (item.kind === "genuinely new") {
      skipped.push(`${item.perBase.map(p => p.base).join(", ")}: genuinely new — kept`);
      continue;
    }
    if (!item.auto) {
      for (const p of item.perBase) {
        const labels = p.candidates.map(b => `[${triggersOf(b).join(" | ")}]`).join("; ");
        skipped.push(`${p.base}: ${p.candidates.length} candidates — choose one: ${labels || "no unique trigger match"}`);
      }
      continue;
    }
    for (const p of item.perBase) merges.push({ base: p.base, match: p.match });
  }
  return { merges, skipped };
}

function suggest(state, args) {
  section(`Suggestions ${state.botId}`);
  const classified = classifyPlaceholders(state);
  if (classified.length === 0) {
    console.log("No placeholders. Nothing to merge; check --changes and --check-inputtest.");
    return;
  }
  for (const item of classified) {
    console.log(`\n[${item.kind}] media: ${item.media.join(", ")}`);
    for (const p of item.perBase) {
      const labels = p.candidates.map(b => `[${triggersOf(b).join(" | ")}]`).join("; ") || "none";
      console.log(`  ${p.base}: ${p.candidates.length} candidate(s) -> ${labels}${p.match ? ` -> match "${p.match}"` : ""}`);
    }
  }
  const { merges, skipped } = suggestedMerges(state);
  const plan = JSON.stringify({ merges }, null, 2);
  console.log(`\nSuggested merge plan:\n${plan}`);
  for (const note of skipped) console.log(`SKIPPED: ${note}`);
  const out = args["plan-out"] ?? `/tmp/${state.botId}-suggested-plan.json`;
  if (args.write || args["plan-out"]) {
    fs.writeFileSync(path.resolve(state.repoRoot, out), plan + "\n", "utf8");
    console.log(`Plan written to ${out}`);
  } else {
    console.log(`Dry run: re-run with --write (or --plan-out <path>) to save the plan.`);
  }
}

function loadInputTest(state, args) {
  if (state.inputTestLines) return true;
  const file = args.inputtest ? path.resolve(state.repoRoot, args.inputtest) : state.inputTestPath;
  if (!fs.existsSync(file)) {
    console.log(`inputTest.txt not found at ${rel(state.repoRoot, file)} — skipped.`);
    return false;
  }
  state.inputTestPath = file;
  state.inputTestRaw = fs.readFileSync(file, "utf8");
  state.inputTestLines = state.inputTestRaw.replace(/\n$/, "").split("\n");
  return true;
}

function resolveInput(bundle, input) {
  if (!bundle?.trigger?.TextTrigger) return null;
  const values = triggersOf(bundle).map(value => {
    const raw = bundle.trigger.TextTrigger.triggers.find(
      v => (v.StringTextTriggerValue ?? v.RegexTextTriggerValue?.trigger) === value
    );
    const isRegex = typeof raw?.RegexTextTriggerValue !== "undefined";
    return {
      value,
      length: isRegex ? raw.RegexTextTriggerValue.regexLength : stringTextLength(value),
      matches: isRegex ? regexMatches(value, input) : input.includes(value),
    };
  });
  if (bundle.matcher === "ContainsAll") {
    if (values.length === 0 || !values.every(v => v.matches)) return null;
    return values.reduce((sum, v) => sum + v.length, 0);
  }
  const matching = values.filter(v => v.matches);
  if (matching.length === 0) return null;
  // doesMatch sorts by trigger length descending and takes the first match.
  return matching.reduce((best, v) => (v.length > best.length ? v : best)).length;
}

function stringTextLength(value) {
  return value.length;
}

function regexMatches(pattern, input) {
  try {
    return new RegExp(pattern).test(input);
  } catch {
    return false;
  }
}

function inputTestIssues(state, args) {
  if (!loadInputTest(state, args)) return [];
  const issues = [];
  for (const [index, line] of state.inputTestLines.entries()) {
    const arrow = line.indexOf(" -> ");
    if (arrow < 0) continue;
    const input = line.slice(0, arrow).toLowerCase();
    const expected = line.slice(arrow + 4).split(",").map(s => s.trim());
    const scored = state.replies
      .map((bundle, bundleIndex) => ({ bundleIndex, bundle, score: resolveInput(bundle, input) }))
      .filter(x => x.score !== null)
      .sort((a, b) => b.score - a.score); // stable: ties keep bundle order
    const best = scored[0];
    if (!best) {
      issues.push({ line: index + 1, input, missing: expected, extra: [], noMatch: true });
      continue;
    }
    const actual = mediaOf(best.bundle);
    const missing = expected.filter(f => !actual.includes(f));
    const extra = actual.filter(f => !expected.includes(f));
    if (missing.length || extra.length) issues.push({ line: index + 1, input, missing, extra, bundleIndex: best.bundleIndex });
  }
  return issues;
}

function checkInputTest(state, args) {
  section(`Check inputTest ${state.botId}`);
  const issues = inputTestIssues(state, args);
  if (issues.length === 0) {
    console.log("All inputTest.txt expectations match the resolved reply bundles.");
    return;
  }
  for (const issue of issues) {
    console.log(`\nline ${issue.line}: ${issue.input}`);
    if (issue.noMatch) console.log("  NO MATCHING BUNDLE");
    if (issue.missing.length) console.log(`  expectation would SHRINK (stop and report): ${issue.missing.join(", ")}`);
    if (issue.extra.length) console.log(`  can ADD to expectation: ${issue.extra.join(", ")}`);
  }
  const shrinking = issues.filter(i => i.missing.length);
  if (shrinking.length) {
    state.errors.push(`${shrinking.length} inputTest expectation(s) would need to shrink — human review required`);
  }
}

function bundleSignature(bundle) {
  // Stable across media edits and placeholder removal: trigger set + matcher only.
  return JSON.stringify([bundle?.matcher ?? "ContainsOnce", [...triggersOf(bundle)].sort()]);
}

function patchInputTest(state, args) {
  if (!loadInputTest(state, args)) return;
  const issues = inputTestIssues(state, args);
  const shrinking = issues.filter(i => i.missing.length);
  if (shrinking.length) {
    throw new Error(
      `inputTest.txt would need to shrink on line(s) ${shrinking.map(i => i.line).join(", ")}. ` +
      "Stop and report to a human (task rule: additions only)."
    );
  }
  // Index-based HEAD lookup is unsafe: dedupe/merge shifts bundle indices.
  // Match by trigger signature so the "before" media set is the right bundle.
  const headBySignature = new Map();
  if (state.headReplies) {
    for (const b of state.headReplies) {
      const sig = bundleSignature(b);
      if (!headBySignature.has(sig)) headBySignature.set(sig, b);
    }
  }
  let patched = 0;
  for (const issue of issues) {
    if (!issue.extra.length) continue;
    const bundle = state.replies[issue.bundleIndex];
    const head = headBySignature.get(bundleSignature(bundle));
    const before = head ? mediaOf(head) : [];
    const newlyAdded = mediaOf(bundle).filter(f => !before.includes(f));
    const toAdd = issue.extra.filter(f => newlyAdded.includes(f));
    const lineIndex = issue.line - 1;
    if (toAdd.length) {
      state.inputTestLines[lineIndex] = state.inputTestLines[lineIndex] + ", " + toAdd.join(", ");
      state.dirtyInputTest = true;
      patched++;
    }
    const oldExtras = issue.extra.filter(f => !newlyAdded.includes(f));
    if (oldExtras.length) state.warnings.push(`inputTest line ${issue.line} has pre-existing stale extras: ${oldExtras.join(", ")}`);
  }
  section(`Patch inputTest ${state.botId}`);
  console.log(patched ? `Extended ${patched} line(s) by addition only.` : "No inputTest changes needed.");
}

function resolveAll(state, args) {
  section(`Resolve ${state.botId}`);
  dedupeList(state, args);
  const { merges, skipped } = suggestedMerges(state);
  if (merges.length) applyPlan(state, args, { merges });
  for (const note of skipped) state.warnings.push(note);
  patchInputTest(state, args);
}

function writeReport(state, args) {
  const out = path.resolve(state.repoRoot, args.report);
  const names = state.list.map(e => e.filename);
  const duplicates = duplicateValues(names);
  const refs = unique(state.replies.flatMap(mediaOf));
  const placeholders = state.replies.filter(isPlaceholder);
  const classified = classifyPlaceholders(state);
  const headNames = state.headList ? new Set(state.headList.map(e => e.filename)) : null;
  const removedNames = headNames ? unique([...headNames].filter(n => !names.includes(n))) : [];
  const removedDetail = duplicates.length || removedNames.length
    ? unique([...duplicates, ...removedNames]).map(n => `  - \`${n}\` (superseded/duplicate entry removed)`).join("\n")
    : "  - None.";
  const newNames = headNames ? unique(names.filter(n => !headNames.has(n))) : [];
  const unreferencedNew = newNames.filter(n => !refs.includes(n));
  const addDataLine = headNames
    ? `- AddData media referenced: ${newNames.length - unreferencedNew.length}/${newNames.length} new list files referenced by reply bundles${unreferencedNew.length ? ` (unreferenced: ${unreferencedNew.join(", ")})` : ""}.`
    : "- AddData media referenced: n/a (HEAD version unavailable).";
  const mergeRows = classified.filter(i => i.auto).flatMap(i =>
    i.perBase.map(p => {
      const target = p.candidates[0];
      const added = i.media.filter(f => baseOf(f, state.botId) === p.base).join(", ");
      return `| \`${p.base}\` | ${triggersOf(target).join(", ")} | ${added} |`;
    })
  ).join("\n") || "| `-` | `-` | `-` |";
  const templatePath = path.join(state.repoRoot, "agent-context", "tools", "conflict-resolution", "report-template.md");
  const template = fs.readFileSync(templatePath, "utf8");
  const filled = template
    .replace("YYYY-MM-DD", new Date().toISOString().slice(0, 10))
    .replace("`<bot ids>`", `\`${state.botId}\``)
    .replace("`<this file>`", `\`${args.report}\``)
    .replace("`<bot>_list.json`", `\`${state.botId}_list.json\``)
    .replace("`<bot>_replies.json`", `\`${state.botId}_replies.json\``)
    .replace("- Removed `<n>` superseded duplicate entries:\n  - `<filename>` (`<old source>`)", `- Removed ${removedNames.length || duplicates.length} superseded/duplicate entries:\n${removedDetail}`)
    .replace("- `<base>`: new media completes/replaces `<existing media set>`.", classified.length
      ? classified.map(i => `- \`${i.perBase.map(p => p.base).join(", ")}\`: ${i.kind}.`).join("\n")
      : "- None detected.")
    .replace("- `<base>`: no existing bundle or media base match; kept as `new data`.", classified
      .filter(i => i.kind === "genuinely new")
      .map(i => `- \`${i.perBase.map(p => p.base).join(", ")}\`: kept as \`new data\`.`).join("\n") || "- None.")
    .replace("- Final count: `<n>` entries, `<n>` duplicate filenames.", `- Final count: ${names.length} entries, ${duplicates.length} duplicate filenames.`)
    .replace("- Removed `<n>` replacement placeholders.", `- Removed ${classified.filter(i => i.kind.startsWith("replacement")).length} replacement placeholder(s).`)
    .replace("| `<base>` | `<triggers>` | `<files>` |", mergeRows)
    .replace("- Final count: `<n>` bundles, `<n>` placeholders.", `- Final count: ${state.replies.length} bundles, ${placeholders.length} placeholders.`)
    .replace("Removed: `<n>` (replacement/completion).", `Removed: ${classified.filter(i => i.kind.startsWith("replacement")).length} placeholder(s) classified as replacement/completion (verify).`)
    .replace("Kept intentionally: `<n>` genuinely new placeholders:", `Kept intentionally: ${classified.filter(i => i.kind === "genuinely new").length} genuinely new placeholders:`)
    .replace("- List duplicate check: `<result>`.", `- List duplicate check: ${duplicates.length === 0 ? "passed" : `FAILED (${duplicates.join(", ")})`}.`)
    .replace("- New AddData media referenced: `<result>`.", addDataLine);
  fs.writeFileSync(out, filled, "utf8");
  console.log(`Report written to ${args.report} (review judgment calls before handoff).`);
}

function validate(state, args) {
  section(`Validate ${state.botId}`);

  if (!args["no-inputtest"]) checkInputTest(state, args);

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

function writeLines(file, lines, original) {
  const trailingNewline = original.endsWith("\n") ? "\n" : "";
  fs.writeFileSync(file, lines.join("\n") + trailingNewline, "utf8");
}

export { serialize, baseOf, mediaTypeOf, mediaOf, triggersOf };
