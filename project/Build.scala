import sbt._

object HStreamzBuild extends Build {
  lazy val root = Project(id = "hstreamz",
                            base = file("."),
                            settings = Project.defaultSettings)
                            
  val zipPath = TaskKey[File]("zip-path", "Path for zip output file.")
    
  val zip = TaskKey[File]("zip","Task to zip up the build")

  def docFiles = TaskKey[Seq[File]]("doc-files", "Array of documention files")
    
}
