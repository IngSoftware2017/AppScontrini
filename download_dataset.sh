mkdir 'GDrive'
mkdir 'GDrive/Ingegneria del software'
mkdir 'GDrive/Ingegneria del software/DataSet'
mkdir 'GDrive/Ingegneria del software/DataSet/photos'
cd 'GDrive/Ingegneria del software/DataSet/photos'
wget --no-check-certificate 'https://docs.google.com/uc?id=11bZwcT1EkxaVor0Ux8Houl9JFD8Tpkoc&export=download' -O '0.jpg'
ls
cd ..
cd ..
cd ..
cd ..
mkdir 'app/src/androidTest/assets'
mkdir 'app/src/androidTest/assets/photos'
cp -avr 'GDrive/Ingegneria del software/DataSet/photos/.' 'app/src/androidTest/assets/photos'

