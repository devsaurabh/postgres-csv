package demo.banking.config

import java.util.Map.Entry

import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigValue}

import scala.util.Try

abstract class Settings[T](prefix: String) {

  def apply(config: Config): T = fromSubConfig(Try(config getConfig prefix).getOrElse(ConfigFactory.empty()))

  def fromSubConfig(c: Config): T

  implicit class ConfigToMap(c: Config) {
    import scala.collection.JavaConverters._
    def toMap(configName: String): Map[String, Object] = {
      val list = c.getObjectList(configName).asScala
      (for {
        item: ConfigObject <- list
        entry: Entry[String, ConfigValue] <- item.entrySet().asScala
        key = entry.getKey
        uri = entry.getValue.unwrapped()
      } yield (key, uri)).toMap

    }
  }

}
