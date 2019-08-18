package demo.banking.dataaccess.provider

import cats.effect.IO
import doobie.free.connection.ConnectionIO

trait DBProvider {
  def mapToResult[T](res: ConnectionIO[T]): IO[T]
}
