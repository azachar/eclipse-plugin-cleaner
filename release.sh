tag=v$1
releaseVersion=$1
shift

#release code to github with the tag
mvn clean install -Prelease -Dtag=$tag -DreleaseVersion=$releaseVersion $*

#create site for tag
git checkout $tag
mvn clean site -Psite,release $*