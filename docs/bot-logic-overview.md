# Bot Logic Overview

This document gives a high-level map of how bots work in this repository, from startup to sending replies.

It is intentionally focused on the runtime path that is most useful while implementing new media behaviors (like markdown files).

## 1) Startup and Wiring

At startup, the app builds one runtime per bot from `BotRegistry`, then each runtime builds its own `BotSetup`.

```mermaid
flowchart TD
  A["Main Runtime"] --> B["BotRegistry.value"]
  B --> C["BotRegistryEntry for each bot"]
  C --> D["TelegramWebhookRuntime.buildWebhookBot"]
  D --> E["BotSetup.apply"]

  E --> F["Read token from resources"]
  E --> G["Load bot config"]
  E --> H["Build DropboxClient"]
  E --> I["Build MegaClient"]
  E --> J["Build DBLayer"]
  E --> K["Build Repository DBRepository.dbResources"]
  E --> L["Build Telegram Api"]
  E --> M["Build BackgroundJobManager"]

  H --> N["HttpClients"]
  I --> N
  N --> K
```

### What matters here

- `BotRegistry` selects which bots are active.
- `BotSetup` wires every dependency used by reply execution.
- `HttpClients` is a dispatcher, not a downloader itself; it chooses Mega vs Dropbox client.

## 2) Incoming Message -> Reply Dispatch

Once a message is parsed into your internal model, reply sending is dispatched by reply type.

```mermaid
flowchart TD
  A["Incoming Telegram Message"] --> B["Compute ReplyValue"]
  B --> C["TelegramMessageReply.sendReplyValue"]

  C --> D{ReplyValue type}
  D -->|Text| E["TextReply.sendText"]
  D -->|MediaFile| F["MediaFileReply.sendMediaFile"]
  D -->|TelegramInlineKeyboard| G["KeyboardReply.sendKeyboard"]
  D -->|Other| H["Warn and return empty"]
```

### Media subtype dispatch

```mermaid
flowchart TD
  A["MediaFileReply.sendMediaFile"] --> B{Media subtype}
  B --> C["Mp3 to sendAudio"]
  B --> D["Gif to sendAnimation"]
  B --> E["Photo to sendPhoto"]
  B --> F["Video to sendVideo"]
  B --> G["Document to sendDocument"]
  B --> H["Sticker to sendSticker"]
  B --> I["MarkdownFile to sendMarkdown"]
```

## 3) How Media Is Resolved From DB Sources

The repository resolves each media file to one or more candidate sources. Sources can be either:

- `Right(Uri)` -> remote URL (Mega/Dropbox), fetched to temp file
- `Left(String)` -> Telegram reusable file reference (mapped to `MediaResourceIFile`)

```mermaid
flowchart TD
  A["MediaFile requested"] --> B["DBRepository.getResourceFile"]
  B --> C["DBMedia.getMedia filepath"]
  C --> D{Found?}
  D -->|No| E["RepositoryError NoResourcesFoundFile"]
  D -->|Yes| F["DBMediaData to Media"]
  F --> G["Iterate media sources"]

  G -->|Right Uri| H["HttpClients.fetchFile"]
  H --> I{Host}
  I -->|dropbox.com| J["DropboxClient.fetchFile"]
  I -->|mega.nz| K["MegaClient.fetchFile"]
  J --> L["MediaResourceFile temp path"]
  K --> L

  G -->|Left string| M["MediaResourceIFile"]

  L --> N["NonEmptyList MediaResource"]
  M --> N
```

## 4) Sending Media: Fallback Behavior

For non-text media sends, the adapter uses a shared pattern (`telegramFileReplyPattern`):

```mermaid
sequenceDiagram
  participant T as TelegramMessageReply
  participant R as Repository
  participant MR as MediaResource
  participant API as Telegram API

  T->>API: sendChatAction
  T->>R: getResourceFile(mediaFile)
  R-->>T: NonEmptyList[MediaResource]

  loop each resource with fallback
    T->>MR: toTelegramApi
    MR-->>T: IFile (InputPartFile or InputLinkFile)
    T->>API: sendX(chatId, IFile, replyParameters)
    alt send fails
      T->>T: log error, try next resource
    end
  end
```

### Why fallback exists

- A media item can have multiple sources.
- If first source fails (expired link, transient network issue), next source is attempted.

## 5) Markdown Current Behavior

Markdown enters the same `MediaFile` branch and is handled by `sendMarkdown` with the current behavior:

- `MediaResourceFile` is read as text and sent via `TextReply.sendText` using markdown text mode.
- `MediaResourceIFile` is explicitly unsupported and raises an exception in `F`; this is then handled by the shared reply pipeline as a failed source.

```mermaid
flowchart TD
  A["MarkdownFile"] --> B["Repository.getResourceFile"]
  B --> C{MediaResource kind}
  C -->|MediaResourceFile| D["Read file content as String"]
  D --> E["TextReply.sendText markdown mode"]
  C -->|MediaResourceIFile| F["raise UnsupportedOperationException in F"]
  F --> G["Shared fallback tries next source or returns empty"]
```

## 6) Practical Mental Model

- `ReplyValue` decides *what* to send.
- `Repository` decides *where content comes from*.
- `MediaResource` decides *how Telegram receives it* (`InputPartFile` vs `InputLinkFile`).
- `TelegramMessageReply` decides *how to execute and fallback*.

If you keep these four responsibilities separate, adding new media behavior is much easier and safer.
