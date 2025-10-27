#! /bin/bash

VERSION="latest"
REGISTRY=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --version)
      VERSION="$2"
      shift 2
      ;;
    --registry)
      REGISTRY="$2"
      shift 2
      ;;
    *)
      shift
      ;;
  esac
done

if [ -z "$REGISTRY" ]; then
  echo "Error: --registry 옵션이 필요합니다."
  echo "Usage: $0 --registry REGISTRY [--version VERSION]"
  exit 1
fi

docker image rmi $REGISTRY/mdt-client:$VERSION

cp ../build/libs/mdt-client-1.2-all.jar mdt-client-all.jar

docker build -t $REGISTRY/mdt-client:$VERSION .
docker push $REGISTRY/mdt-client:$VERSION

rm mdt-client-all.jar
