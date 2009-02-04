# JRI - Java/R Interface      experimental!
#--------------------------------------------------------------------------

#--- comment out the following for non-debug version
CFLAGS+=-g

CC=gcc
CFLAGS+=-Iinclude  -D_FILE_OFFSET_BITS=64 -D_LARGEFILE64_SOURCE -D_LARGEFILE_SOURCE -g -O2 -I/usr/local/share/java/jdk1.5.0/include -I/usr/local/share/java/jdk1.5.0/include/linux
LDFLAGS+=-L/usr/local/share/java/jdk1.5.0/jre/lib/i386/client/

CC=gcc
RHOME=/usr/local/lib/R
JAVAC=/usr/local/share/java/jdk1.5.0/bin/javac $(JFLAGS)
JAVAH=/usr/local/share/java/jdk1.5.0/bin/javah
JAVAINC=-I/usr/local/share/java/jdk1.5.0/include -I/usr/local/share/java/jdk1.5.0/include/linux
JAR=/usr/local/share/java/jdk1.5.0/bin/jar

RINC=-I/usr/local/lib/R/include -I/usr/local/lib/R/include
RLD=-L/usr/local/lib/R/lib -lR
JNILD=-shared  -ljvm
JNISO=.so
JNIPREFIX=lib
CPICF=-fPIC

include Makefile.all
