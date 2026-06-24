#!/usr/bin/env -S scala-cli run
//> using scala "3"
//> using dep "com.lihaoyi::os-lib:0.11.8"

/** Replace prod botDB.sqlite3 with a local sqlite file and restart bots.
  *
  * Usage:
  *   scala-cli scripts/UploadProdDB.scala -- /path/to/local/botDB.sqlite3
  *
  * Required environment variables:
  *   - DEPLOY_SERVER_IP: target server IP / hostname
  *
  * Optional environment variables:
  *   - DEPLOY_SERVER_USER (default: ubuntu)
  *   - DEPLOY_REMOTE_DIR (default: /home/ubuntu/bots)
  *   - SSH_KEY_PATH (default: ~/.ssh/id_rsa)
  */
object UploadProdDB:

  private def fail(message: String): Nothing =
    Console.err.println(s"Error: $message")
    sys.exit(1)

  private def envOrDefault(name: String, default: String): String =
    sys.env.getOrElse(name, default)

  private def runOrFail(command: Seq[String]): Unit =
    val rendered = command.mkString(" ")
    println(s"\n$rendered")
    try os.proc(command).call(check = true, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)
    catch
      case e: os.SubprocessException =>
        fail(s"Command failed with exit code ${e.result.exitCode}: $rendered")

  private def toPath(input: String): os.Path =
    if input.startsWith("/") then os.Path(input, os.root) else os.Path(input, os.pwd)

  @main def main(dbFilePath: String): Unit =
    val dbPath = toPath(dbFilePath)
    if !os.exists(dbPath) || !os.isFile(dbPath) then fail(s"DB file not found: $dbPath")

    val serverIp   = sys.env.getOrElse("DEPLOY_SERVER_IP", fail("DEPLOY_SERVER_IP is required"))
    val serverUser = envOrDefault("DEPLOY_SERVER_USER", "ubuntu")
    val remoteDir  = envOrDefault("DEPLOY_REMOTE_DIR", "/home/ubuntu/bots")
    val sshKeyPath = envOrDefault("SSH_KEY_PATH", s"${sys.props("user.home")}/.ssh/id_rsa")

    val sshBase = Seq(
      "ssh",
      "-o",
      "StrictHostKeyChecking=no",
      "-i",
      sshKeyPath,
      s"$serverUser@$serverIp"
    )

    val rsyncCommand = Seq(
      "rsync",
      "-av",
      "-e",
      s"ssh -o StrictHostKeyChecking=no -i $sshKeyPath",
      dbPath.toString,
      s"$serverUser@$serverIp:$remoteDir/botDB.sqlite3"
    )

    println(s"Uploading DB from: $dbPath")
    println(s"Target: $serverUser@$serverIp:$remoteDir/botDB.sqlite3")

    // Stop running bots before replacing DB file.
    runOrFail(sshBase ++ Seq("sh", "-c", "killall java || true"))

    // Keep a timestamped backup on server for rollback.
    val backupCmd =
      s"""cd "$remoteDir" && cp botDB.sqlite3 "botDB.sqlite3.backup.$$(date +%Y%m%d%H%M%S)" """
    runOrFail(sshBase ++ Seq("sh", "-c", backupCmd))

    // Upload replacement DB from local machine.
    runOrFail(rsyncCommand)

    // Restart bot process.
    val restartCmd =
      s"""cd "$remoteDir" && nohup java -cp main.jar com.benkio.main.MainWebhook > /dev/null 2>&1 &"""
    runOrFail(sshBase ++ Seq("sh", "-c", restartCmd))

    println("\nProd DB replaced and bots restarted successfully.")
