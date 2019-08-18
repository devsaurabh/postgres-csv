import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.common.jimfs.{Configuration, Jimfs}
import demo.banking.dataaccess.Repository.BankRepository
import demo.banking.services.BankRegisterServiceImpl
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class BankRegisterServiceSpec extends FlatSpec with MockFactory with Matchers {
  val fs = Jimfs.newFileSystem("FileSourceSpec", Configuration.unix())
  val repoMock = mock[BankRepository]

  implicit val system = ActorSystem()
  implicit val testContext = system.dispatcher
  implicit val mat = ActorMaterializer()

  behavior of "RegisterBanks"

  it should "not process an invalid csv file" in {
    val service = new BankRegisterServiceImpl(repoMock)
    val resF = service.registerBanks(Paths.get("blah.csv"))
    resF map { res => assert(res === 0)}
  }

  it should "not process invalid records" in {
    val service = new BankRegisterServiceImpl(repoMock)
    val testFile = getInvalidFile("not-process")
    val resF = service.registerBanks(testFile)
    resF map { res => assert(res === 0)}
    deleteFile(testFile)
  }

  it should "not insert duplicate record in db" in {
    val service = new BankRegisterServiceImpl(repoMock)
    val testFile = getDuplicateCsv("duplicate_data")

    val resF = service.registerBanks(testFile)
    resF map { res => assert(res === 0)}

    deleteFile(testFile)
  }

  it should "insert valid records to db" in {
    val service = new BankRegisterServiceImpl(repoMock)
    val testFile = getInvalidFile("valid_data")

    val resF = service.registerBanks(testFile)
    resF map { res => assert(res === 1)}

    deleteFile(testFile)
  }

  private def getDuplicateCsv(fileName: String): Path = {
    val duplicateCsv = new StringBuilder
    duplicateCsv.append("name;bank_identifier\n")
    duplicateCsv.append("Postbank;10010010\n")
    duplicateCsv.append("Postbank;10010010\n")

    getTestFile(duplicateCsv, fileName)
  }

  private def getValidCsv(fileName: String): Path = {
    val duplicateCsv = new StringBuilder
    duplicateCsv.append("name;bank_identifier\n")
    duplicateCsv.append("Postbank;10010010\n")

    getTestFile(duplicateCsv, fileName)
  }

  private def getInvalidFile(fileName: String): Path = {
    val duplicateCsv = new StringBuilder
    duplicateCsv.append("name;bank_identifier\n")
    duplicateCsv.append("Postbank0\n")

    getTestFile(duplicateCsv, fileName)
  }

  private def getTestFile(data: StringBuilder, fileName:String): Path = {
    val f = Files.createTempFile(fs.getPath("/"), fileName, ".tmp")
    val p = f.getFileName
    Files.newBufferedWriter(f, UTF_8).append(data).close()
    f
  }

  private def deleteFile(file: Path) = {
    Files.delete(file)
  }
}
