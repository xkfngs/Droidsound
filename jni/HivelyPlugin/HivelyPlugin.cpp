
#include <stdlib.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>

#include "com_ssb_droidsound_plugins_HivelyPlugin.h"

extern "C" {
#include "hvl_replay.h"
}

#define INFO_TITLE 0
#define INFO_AUTHOR 1
#define INFO_LENGTH 2
#define INFO_TYPE 3
#define INFO_COPYRIGHT 4
#define INFO_GAME 5
#define INFO_SUBTUNES 6
#define INFO_STARTTUNE 7
#define INFO_SUBTUNE_TITLE 8

static jstring NewString(JNIEnv *env, const char *str)
{
	static jchar *temp, *ptr;

	temp = (jchar *) malloc((strlen(str) + 1) * sizeof(jchar));

	ptr = temp;
	while(*str) {
		unsigned char c = (unsigned char)*str++;
		*ptr++ = (c < 0x7f && c >= 0x20) || c >= 0xa0 || c == 0xa ? c : '?';
	}
	//*ptr++ = 0;
	jstring j = env->NewString(temp, ptr - temp);

	free(temp);

	return j;
}

static bool inited = false;


JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_plugins_HivelyPlugin_N_1load(JNIEnv *env, jobject obj, jstring fname)
{
	if(!inited) {
		hvl_InitReplayer();
		inited = true;
	}

	const char *filename = env->GetStringUTFChars(fname, NULL);
	
	char temp[260];
	strcpy(temp, filename);
	
	struct hvl_tune *tune = hvl_LoadTune(temp, 44100, 0);
	if (!tune)
	{
		return -1;
	}
		
	return (jlong)tune;
}


JNIEXPORT void JNICALL Java_com_ssb_droidsound_plugins_HivelyPlugin_N_1unload(JNIEnv *env, jobject obj, jlong song)
{
	struct hvl_tune *tune = (struct hvl_tune *)song;
	hvl_FreeTune(tune);
}


JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_HivelyPlugin_N_1getSoundData(JNIEnv *env, jobject obj, jlong song, jshortArray bArray, int size)
{
	struct hvl_tune *tune = (struct hvl_tune *)song;

	jshort *dest = env->GetShortArrayElements(bArray, NULL);

	int8 *ptr = (int8 *)dest;

	int len = ((44100*2)/5);

	for(int i=0; i<10; i++) 
	{
		hvl_DecodeFrame(tune, ptr, ptr+2, 4);
		ptr += ((44100*2*2)/50);
	}

	env->ReleaseShortArrayElements(bArray, dest, 0);
	return len;
}

