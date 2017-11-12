[![Build Status](https://travis-ci.org/Kraktun/AppScontrini.svg?branch=K_main)](https://travis-ci.org/Kraktun/AppScontrini)

## BRANCH PER TEST PERSONALE  
« Per me si va ne la città dolente,  
per me si va ne l'etterno dolore,  
per me si va tra la perduta gente.  
Giustizia mosse il mio alto fattore:  
fecemi la divina potestate,  
la somma sapienza e 'l primo amore;  
dinanzi a me non fuor cose create  
se non etterne, e io etterna duro.  
Lasciate ogni speranza, voi ch'entrate. »  

## FUNZIONAMENTO ANALISI OCR:
I metodi realizzati finora sono abbastanza basilari:  
* [analyzeSingleText()](https://github.com/Kraktun/AppScontrini/blob/K_main/app/src/main/java/com/ing/software/appscontrini/OCR/OcrAnalyzer.java#L43)  
	Analizza la foto e riporta sotto forma di stringa tutto ciò che trova, ordinando i blocchi dall'alto verso il basso e da sinistra a destra.  
	La stringa è costruita andando a capo a ogni Text contenuto nel blocco, per cui la stringa finale risulta una lista di tutti i Text della foto.  
	Nota: Sono ordinati solo i blocchi, quindi la lista può risultare complessivamente non ordinata.  
* [analyzeBruteFirstString()](https://github.com/Kraktun/AppScontrini/blob/K_main/app/src/main/java/com/ing/software/appscontrini/OCR/OcrAnalyzer.java#L82)  
	Analizza la foto e cerca il primo Text contenente la stringa passata come parametro. L'ordinamento (dall'alto verso il basso e da sinistra a destra) è eseguito solo sui blocchi, quindi il primo match non è detto che sia effettivamente nel primo Text.  
	Restituisce una stringa contenente il testo contenuto nel Text individuato.  
* [analyzeBruteContinuousString()](https://github.com/Kraktun/AppScontrini/blob/K_main/app/src/main/java/com/ing/software/appscontrini/OCR/OcrAnalyzer.java#L123)  
	Analizza la foto e cerca tutti i Text contenenti la stringa passata come parametro. Restituisce una lista contenente tutti i Text che contengono quella stringa.  
	L'ordinamento (dall'alto verso il basso e da sinistra a destra) è eseguito solo sui blocchi, quindi la lista può risultare complessivamente non ordinata.  
* [analyzeBruteContHorizValue()](https://github.com/Kraktun/AppScontrini/blob/K_main/app/src/main/java/com/ing/software/appscontrini/OCR/OcrAnalyzer.java#L172)  
	Analizza la foto e cerca tutti i Text contenenti la stringa passata come parametro. Per ognuno di questi Text:  
		- estrae il rettangolo del Text  
		- estende la larghezza del rettangolo a quella della foto  
		- estende l'altezza del rettangolo in entrambi i sensi della metà della percentuale passata come parametro (ad esempio se si passa come precisione p e il rettangolo è alto h, questo verrà alzato di h*p/100 (verso l'alto e h*p/100 verso il basso))  
		- cerca altri Text contenuti nel nuovo rettangolo  
	Restituisce una lista contenente sia i Text con la stringa (che ovviamente sono contenuti nel rettangolo esteso), sia eventuali altri Text contenuti nel rettangolo esteso.  
	La lista è non ordinata, nel senso i Text individuati in uno stesso rettangolo esteso sono "vicini", ma seguono l'ordinamento dei blocchi.  

## TO DO	 
* Inserire ordinamento per Text (è già scritto basta implementarlo)  
* Utilizzare griglie di decisione  
	
	
