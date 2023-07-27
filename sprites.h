#ifndef SPRITES_H
#define SPRITES_H

#include "globalDefs.h"
#include "debug.h"

#define NO_SPRITE 0
#define NUM_SPRITES 12
#define SUBPIXEL_BITMASK 0b00001111
#define SPRITE_INITIALIZING_BITMASK 0b00010000
#define SPRITE_SPECIFIC_ATTRIBUTE_1_BITMASK 0b00001000
#define SPRITE_SPECIFIC_ATTRIBUTE_2_BITMASK 0b00000100
#define SUBPIXELS_PER_PIXEL 16

/*sprite ids*/
#define PLAYER 1
#define SPRITE_ZERO 2

#define SPRITE_ZERO_INDEX 0
#define PLAYER_SPRITE_INDEX 1

typedef void (*spriteHandler)(void); /*a handler for sprites, must not use global_i*/

typedef union {
	
	struct { unsigned char attrib1, attrib2; };
	unsigned short longAttrib;
} spriteAttribute;

typedef struct {

	signed char offsetX, offsetY;
	unsigned char sizeX, sizeY;
} boundingBox;

unsigned char spritePositionsX[NUM_SPRITES];
unsigned char spritePositionsY[NUM_SPRITES];
signed char spriteSubPixelsX[NUM_SPRITES];
signed char spriteSubPixelsY[NUM_SPRITES];
signed char spriteVelocitiesX[NUM_SPRITES];
signed char spriteVelocitiesY[NUM_SPRITES];
unsigned char spriteIds[NUM_SPRITES];
unsigned char spriteOamAttributes[NUM_SPRITES];
spriteAttribute spriteAttributes[NUM_SPRITES];

#pragma bss-name(push, "ZEROPAGE")

unsigned char currentSprite;
unsigned char spriteShuffler[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };

#pragma bss-name(pop)

#pragma region Sprite Handlers

void noSpriteUpdate(void) {}

void updatePlayer(void) {
	
	if(spriteOamAttributes[currentSprite] & SPRITE_INITIALIZING_BITMASK) {

		spriteOamAttributes[currentSprite] &= ~SPRITE_INITIALIZING_BITMASK; /*no initialization required*/
	}
	
	spriteVelocitiesX[currentSprite] = (controller1 & PAD_RIGHT) << 4;
	spriteVelocitiesX[currentSprite] -= (controller1 & PAD_LEFT) << 3;
	
	printNumber(spriteVelocitiesX[currentSprite], 10, 14);

	spriteVelocitiesY[currentSprite] = (controller1 & PAD_DOWN) << 2;
	spriteVelocitiesY[currentSprite] -= (controller1 & PAD_UP) << 1;
	
	printNumber(spriteVelocitiesY[currentSprite], 10, 15);
}

#pragma endregion

const unsigned char playerMetasprite[] = {

	-8, -16, 0x00, 1,
	-8, -8,  0x10, 1,
	 0, -8,  0x10, 1 | OAM_FLIP_H,
	 0, -16, 0x00, 1 | OAM_FLIP_H,
	128
};

const unsigned char spriteZeroMetasprite[] = {

	0, 0, 0x01, 0,
	128
};

const spriteHandler spriteHandlerJumpTable[] = {
	
	noSpriteUpdate, /*index 0*/
	updatePlayer, /*index 1*/
	noSpriteUpdate /*index 2*/
};

const unsigned char* metaspriteDataPointers[] = {
	
	NULL, /*index 0*/
	playerMetasprite, /*index 1*/
	spriteZeroMetasprite /*index 2*/
};

/*uses global_i and global_j*/
void updateSprites(void) {
	
	static unsigned char temp1, temp2; /*temp variables for assembly stuff*/
	for(global_i = 0; global_i < NUM_SPRITES; ++global_i) {
		
		currentSprite = spriteShuffler[global_i];
		if(spriteIds[currentSprite] == NO_SPRITE) continue; /*ignore if an empty sprite slot*/
		
		/*update the position and subpixels for each sprite*/
		__asm__ ("ldx %v", currentSprite); /*load the x register with the current sprite id*/
		
		/*spriteSubPixelsX[currentSprite] += spriteVelocitiesX[currentSprite];*/
		__asm__ ("lda %v,x", spriteVelocitiesX);
		__asm__ ("sta %v", temp1);
		__asm__ ("lda %v,x", spriteSubPixelsX);
		__asm__ ("clc");
		__asm__ ("adc %v", temp1);
		__asm__ ("sta %v,x", spriteSubPixelsX);
		
		/*spritePositionsX[currentSprite] += spriteSubPixelsX[currentSprite] >> 4;*/
		/*don't need to get spriteSubPixelsX[currentSprite] again, because it's already in the accumulator*/
		//__asm__ ("lsr a");
		//__asm__ ("lsr a");
		//__asm__ ("lsr a");
		//__asm__ ("lsr a");
		__asm__ ("stx %v", temp2);
		__asm__ ("jsr asrax4");
		__asm__ ("sta %v", temp1);
		__asm__ ("ldx %v", temp2);
		__asm__ ("lda %v,x", spritePositionsX);
		__asm__ ("clc");
		__asm__ ("adc %v", temp1);
		__asm__ ("sta %v,x", spritePositionsX);
		
		/*spriteSubPixelsX[currentSprite] &= SUBPIXEL_BITMASK;*/
		__asm__ ("lda %v,x", spriteSubPixelsX);
		__asm__ ("and #%b", SUBPIXEL_BITMASK);
		__asm__ ("sta %v,x", spriteSubPixelsX);
		
		/*spriteSubPixelsY[currentSprite] += spriteVelocitiesY[currentSprite];*/
		__asm__ ("lda %v,x", spriteVelocitiesY);
		__asm__ ("sta %v", temp1);
		__asm__ ("lda %v,x", spriteSubPixelsY);
		__asm__ ("clc");
		__asm__ ("adc %v", temp1);
		__asm__ ("sta %v,x", spriteSubPixelsY);
		
		/*spritePositionsY[currentSprite] += spriteSubPixelsY[currentSprite] >> 4;*/
		__asm__ ("clc");
		__asm__ ("lsr a");
		__asm__ ("lsr a");
		__asm__ ("lsr a");
		__asm__ ("lsr a");
		__asm__ ("sta %v", temp1);
		__asm__ ("lda %v,x", spritePositionsY);
		__asm__ ("clc");
		__asm__ ("adc %v", temp1);
		__asm__ ("sta %v,x", spritePositionsY);
		
		/*spriteSubPixelsY[currentSprite] &= SUBPIXEL_BITMASK;*/
		__asm__ ("lda %v,x", spriteSubPixelsY);
		__asm__ ("and #%b", SUBPIXEL_BITMASK);
		__asm__ ("sta %v,x", spriteSubPixelsY);
		
		spriteHandlerJumpTable[spriteIds[currentSprite]](); /*run the sprite id-specific code for the sprite*/
		oam_meta_spr(spritePositionsX[currentSprite], spritePositionsY[currentSprite], metaspriteDataPointers[spriteIds[currentSprite]]); /*draw the sprite*/
	}

	/*shuffle sprites*/
	global_j = spriteShuffler[NUM_SPRITES - 1]; /*yes, I'm using global_j for something other than indexing and who's going to stop me (that's a rhetorical question)*/
	for(global_i = sizeof(spriteShuffler) / sizeof(unsigned char) - 1; global_i > 0; --global_i) {
		
		spriteShuffler[global_i] = spriteShuffler[global_i - 1];
	}

	spriteShuffler[0] = global_j;
}

/*uses global_j, attributes must include SPRITE_INITIALIZING, puts return value in currentSprite*/
void initializeSprite(unsigned char id, unsigned char positionX, unsigned char positionY, unsigned char oamAttributes) {
	
	for(currentSprite = 2; currentSprite < NUM_SPRITES; ++currentSprite) { /*sprites 0 and 1 are sprite zero and the player so they won't ever be reinitialized*/
		
		if(spriteIds[currentSprite] == NO_SPRITE) {
			
			spritePositionsX[currentSprite] = positionX;
			spritePositionsY[currentSprite] = positionY;
			spriteVelocitiesX[currentSprite] = 0;
			spriteVelocitiesY[currentSprite] = 0;
			spriteIds[currentSprite] = id;
			spriteIds[oamAttributes] = oamAttributes;
			return /*currentSprite*/;
		}

        ++currentSprite;
	}
	
	currentSprite = -1;
}

#endif