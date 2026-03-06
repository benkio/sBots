package com.benkio.chatcore.model

enum MessageType {
  case Message     extends MessageType
  case Command     extends MessageType
  case FileRequest extends MessageType
}
