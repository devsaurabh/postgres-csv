package demo.banking.config

import com.typesafe.config.Config
import demo.banking.config

case class DBConfig(connection: String, poolSize: Int, credentials: DBCredentials)

case class DBCredentials(user: String, password: String)

object DBConfig extends Settings[DBConfig]("dbConfig") {

  override def fromSubConfig(c: Config): DBConfig = {
    val url = c.getString("url")
    val threadPool = c.getInt("threadPoolSize")
    val username = c.getString("username")
    val password = c.getString("password")
    DBConfig(url, threadPool, DBCredentials(username, password))
  }

  def apply(): DBConfig = apply(config.appConfig)
}
