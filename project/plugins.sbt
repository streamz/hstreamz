
resolvers += "scala-tools.org"  at "http://scala-tools.org/repo-releases"

resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "codehaus" at "http://repository.codehaus.org/org/codehaus/mojo"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0-SNAPSHOT")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.0")

//add dependencies to jar
////https://github.com/sbt/sbt-assembly
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.7")

