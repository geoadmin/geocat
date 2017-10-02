#!/bin/bash

DEST_DIR="/mnt/s3"

usage () {
cat <<EOF
Usage: $0 <BUCKET_NAME> [<BUCKET2_NAME> ...]
  Will run s3cmd sync from s3://<BUCKET_NAME>/ to ${DEST_DIR}/<BUCKET_NAME>/
  for each <BUCKET_NAME> given in argument
EOF
}

if [ $# -eq 0 ]; then
  usage
  exit 1
fi

for BUCKET_NAME in "$@"
do
  echo "Will attempt to sync whole bucket 's3://${BUCKET_NAME}/' to '${DEST_DIR}/${BUCKET_NAME}/'"
  aws s3 sync "s3://${BUCKET_NAME}/" "${DEST_DIR}/${BUCKET_NAME}/"
  ret=$?
done
# ugly workaround for bug [Errno 21] Is a directory: u'/path/to/local/sync/dir' : fake exit status
# https://forums.aws.amazon.com/message.jspa?messageID=331708
case $ret in
  1)
    echo "'aws s3 sync ...' command exited with status $ret, but it may be just a warning, so I exit with status 0"
    exit 0
    ;;
  *)
    exit $ret
    ;;
esac
