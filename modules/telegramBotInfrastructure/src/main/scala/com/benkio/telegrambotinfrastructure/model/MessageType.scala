package com.benkio.telegrambotinfrastructure.model

enum MessageType:
  case Message     extends MessageType
  case Command     extends MessageType
  case FileRequest extends MessageType
