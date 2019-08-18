package demo.banking

import com.typesafe.config.{Config, ConfigFactory}

package object config {
  val appConfig: Config = ConfigFactory.load()
}
