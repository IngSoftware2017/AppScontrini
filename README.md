[![Build Status](https://travis-ci.org/Kraktun/AppScontrini.svg?branch=gruppo2)](https://travis-ci.org/Kraktun/AppScontrini)

## FUNZIONAMENTO ANALISI OCR:
I metodi realizzati finora sono abbastanza basilari:  
* [orderBlocks()](https://github.com/Kraktun/AppScontrini/blob/gruppo2/app/src/main/java/com/ing/software/ticketapp/OCR/OcrAnalyzer.java#L94)  
	Ordina i blocchi sparsi ricevuti dal detector in una lista seguendo l'ordinamento alto -> basso, sinistra -> destra  
	Nota: Sono ordinati solo i blocchi, quindi i RawText possono risultare complessivamente non ordinati.  
* [searchFirstString()](https://github.com/Kraktun/AppScontrini/blob/gruppo2/app/src/main/java/com/ing/software/ticketapp/OCR/OcrAnalyzer.java#L116)  
	Cerca il primo RawText contenente la stringa passata come parametro nella lista ordinata da orderBlocks(). L'ordinamento (dall'alto verso il basso e da sinistra a destra) è eseguito solo sui blocchi, quindi il primo match non è detto che sia effettivamente nel primo Text.  
	Restituisce il primo RawText individuato.  
* [searchContinuousString()](https://github.com/Kraktun/AppScontrini/blob/gruppo2/app/src/main/java/com/ing/software/ticketapp/OCR/OcrAnalyzer.java#L138)  
	Cerca tutti i RawText contenenti la stringa passata come parametro. Restituisce una lista contenente tutti i RawText che contengono quella stringa.  
	L'ordinamento (dall'alto verso il basso e da sinistra a destra) è eseguito solo sui blocchi, quindi la lista può risultare complessivamente non ordinata.  
* [searchContinuousStringExtended()](https://github.com/Kraktun/AppScontrini/blob/gruppo2/app/src/main/java/com/ing/software/ticketapp/OCR/OcrAnalyzer.java#L168)  
	Riceve una lista dei RawText contenenti la stringa passata come parametro. Per ognuno di questi RawText:  
		- estrae il rettangolo del RawText  
		- estende la larghezza del rettangolo a quella della foto  
		- estende l'altezza del rettangolo in entrambi i sensi della metà della percentuale passata come parametro (ad esempio se si passa come precisione p e il rettangolo è alto h, questo verrà alzato di h*p/100 (verso l'alto e h*p/100 verso il basso))  
		- cerca altri RawText contenuti nel nuovo rettangolo  
	Restituisce una lista contenente sia i RawText con la stringa (che ovviamente sono contenuti nel rettangolo esteso), sia eventuali altri RawText contenuti nel rettangolo esteso.  
	La lista è non ordinata, nel senso i RawText individuati in uno stesso rettangolo esteso sono "vicini", ma seguono l'ordinamento dei blocchi.  

## TO DO	 
- [x] Inserire ordinamento per RawText (Ora sono comparable)  
- [x] Inizializzare griglie di decisione  
- [ ] Applicare analisi a griglie di decisione  
