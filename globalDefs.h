#ifndef GLOBAL_DEFS_H
#define GLOBAL_DEFS_H

#define TRUE 1
#define FALSE 0

#define BLACK 0x0f
#define DARK_GREY 0x00
#define LIGHT_GREY 0x10
#define WHITE 0x30

#define SCREEN_SIZE_X 32
#define SCREEN_SIZE_Y 30
#define NUMBER_TO_TILE 48

typedef union {

	unsigned short seed;
	struct {

		unsigned char randByte1;
		unsigned char randByte2;
	};
} rngSeed; /*allows setting the seed and accessing both bytes easily*/

/*remove before release*/
#define DEBUG

#pragma bss-name(push, "ZEROPAGE")

unsigned char global_i, global_j; /*for use by loops, in the zeropage, make sure not to have conflicts!*/

unsigned char controller1, controller1Prev;
unsigned char frameCount;
unsigned char frameCountOverflow;

rngSeed rng;

#pragma bss-name(pop)

#endif