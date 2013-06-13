import AssemblyKeys._ // scala assembly plugin
import sbt.Package.ManifestAttributes

organization := "io.streamz"

name := "hstreamz"

version := "0.0.1"

scalaVersion := "2.10.0"

publishMavenStyle := true

publishTo := Some(Resolver.file("local maven repo",  new File(Path.userHome.absolutePath+"/.m2/repository")))

resolvers += "cloudera"  at "https://repository.cloudera.com/artifactory/repo/"

resolvers += "scala-tools.org releases"  at "https://oss.sonatype.org/content/groups/scala-tools"

resolvers += "codehaus-repo"  at "http://repository.codehaus.org"

resolvers += "Sonatype" at "http://oss.sonatype.org/content/repositories/releases"

resolvers += "local repo" at "file://" + Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
   "io.streamz" % "streamz_2.10" % "0.0.1",
   "org.slf4j" % "slf4j-api" % "1.6.4",
   "com.github.scopt"  % "scopt_2.10" % "2.1.0",
   "org.apache.avro" % "avro" % "1.7.4" % "provided",
   "org.apache.hadoop" % "hadoop-annotations" % "2.0.0-cdh4.2.0" % "provided",
   "org.apache.hadoop" % "hadoop-common" % "2.0.0-cdh4.2.0" % "provided" notTransitive()
)

//do assembly stuff
assemblySettings

mainClass in assembly := Some("io.streamz.hstreamz.Main")

zipPath <<= (target, name, version) map { (t: File, name: String, version: String) =>  t / (name + "-" + version + ".zip")}

docFiles <<= (baseDirectory / "README.md") map { (t:File) => Seq(t) }

zip <<= (baseDirectory,name,version,assembly,docFiles,zipPath) map {
  (base :File, projName: String, versionName: String, jar: File, docs: Seq[File], out: File) =>
    val origInputs: Seq[(File,String)] =
      //rename jar to non-assembly name to keep consistent with maven
      ((Seq(jar) x Path.flat) map {e:(File,String) => (e._1,projName + "-" + versionName + ".jar")}) ++
      (docs x Path.flat)
    //rebase path to 'hstreamz' in zip. easier way above?
    val hstreamzInputs: Seq[(File,String)] = origInputs map { e:(File,String) => (e._1,"hstreamz/" ++ e._2)}
    IO.zip(hstreamzInputs, out)
    out
}


