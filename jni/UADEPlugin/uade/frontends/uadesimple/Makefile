
CC = gcc
CFLAGS = -Wall -O2 -I../../include -I../common   -g 
CLIBS = -lao -lpthread -ldl    -lm

all:	uadesimple

include ../common/Makefile.common

MODULES = uadesimple.o playloop.o audio.o $(COMMONMODULES)

uadesimple:	$(MODULES)
	$(CC) $(CFLAGS) -o $@ $(MODULES) $(CLIBS)

clean:	
	rm -f uadesimple *.o

audio.o:	audio.c audio.h
	$(CC) $(CFLAGS) -c $<

chrarray.o:	chrarray.c chrarray.h
	$(CC) $(CFLAGS) -c $<

playlist.o:	playlist.c playlist.h uadesimple.h ../../include/uadeconfig.h
	$(CC) $(CFLAGS) -c $<

playloop.o:	playloop.c playloop.h uadesimple.h ../common/uadecontrol.h ../../include/uadeipc.h ../common/songinfo.h $(PLAYERHEADERS)
	$(CC) $(CFLAGS) -c $<

terminal.o:	terminal.c terminal.h
	$(CC) $(CFLAGS) -c $<

uadesimple.o:	uadesimple.c uadesimple.h ../../include/uadeipc.h ../../include/amigafilter.h ../common/amifilemagic.h ../common/uadecontrol.h $(PLAYERHEADERS)
	$(CC) $(CFLAGS) -c $<
