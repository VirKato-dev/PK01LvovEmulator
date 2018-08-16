PK-01 Lvov Emulator
===================

PK-01 Lvov (PK-01 Lviv) Computer Emulator

PK-01 Lvov (PK-01 Lviv) was a Soviet-Ukrainian 8-bit personal computer produced from 1986 to 1991.
It had KR580VM80A 2.5-MHz microprocessor (a clone of the Intel 8080 CPU), 64-KB RAM (including 16-KB of video memory with resolution of 256x256 pixels) and was able to display 4 colors from 8-color palette.
It also had BASIC programming-language interpreter built-in.

The emulator is written in Java and based on the work of Hard Wisdom (Vladimir Kalashnikov) with some modifications in sound system and UI.

Build with:

    mvn install

Run with:

    java -jar PK01LvovEmulator.jar

For keyboard layout reference see the "keyboard.png" file.

External programs (*.LVT files) can be loaded using "Load program" from the menu.
You can also import a BASIC program (like "sample.bas"), edit it in internal editor and run it by typing "RUN" (or just by pressing NumLock).

The emulator is distributed under the terms of GNU General Public License version 2.

------------------------------------------------------------------------------------

Эмулятор ПК-01 Львов
====================

Эмулятор компьютера ПК-01 Львов (ПК-01 Львів)

ПК-01 Львов (ПК-01 Львів) - 8-разрядный персональный компьютер, разработанный в УССР и выпускавшийся с 1986 по 1991 гг.
Он работал на микропроцессоре КР580ВМ80А (клон Intel 8080), имел 64-КБ ОЗУ (включая 16-КБ видеопамяти с резолюцией в 256x256 точек) и мог отображать 4 цвета из 8-цветовой палитры.
Был оснащён интерпретатором языка BASIC.

Эмулятор написан на Java. За основу взят эмулятор разработанный Hard Wisdom (Владимир Калашников) с несколько изменёнными звуковой системой и UI.

Для сборки:

    mvn install

Для запуска:

    java -jar PK01LvovEmulator.jar

Раскладка клавиатуры указана в файле "keyboard.png".

Внешние программы (*.LVT файлы) можно загрузить с помощью "Load program" из меню.
Также можно импортировать программу на BASIC (например, "sample.bas"), отредактировать ее во внутреннем редакторе и запустить, набрав "RUN" (или просто нажав NumLock).

Эмулятор распространяется на условиях GNU General Public License version 2.
