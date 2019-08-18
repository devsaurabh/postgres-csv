package demo.banking.dataaccess.provider

import cats.effect.{ContextShift, IO, Resource}
import cats.implicits._
import demo.banking.config.DBConfig
import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._

object PostgresTransactor {

  import doobie.util.ExecutionContexts

  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  lazy val settings = DBConfig()
  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  lazy val transactor: Resource[IO, HikariTransactor[IO]] =
  for {
    ce <- ExecutionContexts.fixedThreadPool[IO](settings.poolSize) // our connect EC
    te <- ExecutionContexts.cachedThreadPool[IO] // our transaction EC
    xa <- HikariTransactor.newHikariTransactor[IO](
      driverClassName = "org.postgresql.Driver",
      settings.connection,
      settings.credentials.user,
      settings.credentials.password,
      ce, // await connection here
      te // execute JDBC operations here
    )
  } yield xa

  def createTable: Unit = {
    val drop =
      sql"""
    DROP TABLE IF EXISTS bank
  """.update.run

    val create =
      sql"""
    CREATE TABLE bank (
      identifier Integer PRIMARY KEY,
      name VARCHAR(50) NOT NULL
    )
  """.update.run

    transactor.use((drop, create).mapN(_ + _).transact(_)).unsafeRunSync
  }
}

class PostgresDbProvider extends DBProvider {
  override def mapToResult[T](res: ConnectionIO[T]): IO[T] = {
    PostgresTransactor.transactor
      .use(res.transact(_))
  }
}
