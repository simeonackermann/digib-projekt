# Projekt 10 - Out of the Box Kalibrierung der Frontkamera

Erkennen eines Schachbrettes und anschließende projektive Transformation.

## Installation

Den Ordner Projekt_10 in das ImageJ Plugin Verzeichnis kopieren.

Die enthaltenen `*.class` files sind unter Linux 64 Bit kompiliert. Wenn das System eine andere Architektur verwendet, müssen sie neu kompiliert werden (auch in den enthaltenen Unterordnern).


## Aufruf

Das Plugin kan von der Konsole sowie im geöffneten ImageJ vom Menü aufgerufen werden.

## vom ImageJ Menü

Nach der Installation kann das Plugin im Untermenü 'Projekt 10' im Plugins-Menü aufgerufen werden. Es muss bereits ein Bild geöffnet sein.

### von der Konsole

Zur Transformation mehrere Bilder kann das Plugin von der Konsole aufgerufen werden. Dafür muss ein Quell-Ordner mit den zu transformierenden Bilder sowie ein Ziel-Ordner angegebene werden.

```
./ImageJ --run Projekt10_ "source=/pfad/zum/quellordner/ result=/pfad/zum/ergebnisordner"
```

