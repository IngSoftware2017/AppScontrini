#!/usr/bin/env bash

# Download a dummy photo
GDRIVE_DIR='GDrive/Ingegneria del software/DataSet/photos'
mkdir -p $GDRIVE_DIR
pushd $GDRIVE_DIR
	wget --no-check-certificate 'https://docs.google.com/uc?id=11bZwcT1EkxaVor0Ux8Houl9JFD8Tpkoc&export=download' -O '0.jpg'
	ls
popd

# Copy photo where needed
PATHS = ('ocr/src/androidTest' 'ocrtestapp/src/main')
for p in $DESTS; do
	mkdir -p "${p}/assets/photos"
	cp -avr "${GDRIVE_DIR}/." "${p}/assets/photos"
done



