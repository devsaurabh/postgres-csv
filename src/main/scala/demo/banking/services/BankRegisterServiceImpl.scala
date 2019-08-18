package demo.banking.services

import java.nio.file.{Files, Path}

import akka.NotUsed
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Flow, Sink}
import demo.banking.dataaccess.Repository.BankRepository
import demo.banking.dataaccess.model.BankInformation

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BankRegisterServiceImpl(bankRepository: BankRepository)
                             (implicit ec: ExecutionContext, mat: ActorMaterializer)
  extends BankRegisterService {

  override def registerBanks(csvFile: Path): Future[Int] = {
    if (!Files.exists(csvFile)) {
      println("File doesn't exists")
      Future.successful(0)
    } else {
      FileIO.fromPath(csvFile)
        .via(CsvParsing.lineScanner(delimiter = ';'))
        .via(CsvToMap.toMapAsStrings())
        .via(BankRegisterServiceImpl.mapToBankInfo)
        .via(registerBankFlowAsync)
        .runWith(BankRegisterServiceImpl.sumInsertedBanksSink)
    }
  }

  override def getBankInfo(bankIdentifier: Int): Future[Option[BankInformation]] = {
    bankRepository.getBankInfo(bankIdentifier)
  }

  private val registerBankFlowAsync: Flow[Option[BankInformation], Int, NotUsed] =
    Flow[Option[BankInformation]]
    .mapAsync(5) {
      case None =>
        println("Invalid row. Skipping")
        Future.successful(0)
      case Some(value: BankInformation) =>
        val res: Future[Either[String, BankInformation]] = bankRepository.safeInsertBankInfo(value)
        res.flatMap {
          case Left(error) =>
            println(s"Id: ${value.identifier}. $error")
            Future.successful(0)
          case Right(_) => Future.successful(1)
        }
    }
}

object BankRegisterServiceImpl {
  /**
    * Flow to map the Map of csv data to bank information
    */
  private val mapToBankInfo: Flow[Map[String,String], Option[BankInformation], NotUsed] =
    Flow[Map[String,String]]
        .map(t=>
          Try(Some(BankInformation(t("bank_identifier").toInt, t("name"))))
            .getOrElse(None)
        )

  /**
    * Sink to sum all the records inserted
    */
  private val sumInsertedBanksSink = Sink.fold[Int,Int](0)(_ + _)
}