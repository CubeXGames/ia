#include "lib/neslib.h"
#include "lib/nesdoug.h"
#include "asm.h"

#define TRUE 1
#define FALSE 0

#define BLACK 0x0f
#define DARK_GREY 0x00
#define LIGHT_GREY 0x10
#define WHITE 0x30

#define NO_SPRITE 0
#define NUM_SPRITES 8
#define SUBPIXEL_BITMASK 0b00001111
#define SPRITE_INITIALIZING_BITMASK 0b00010000
#define SPRITE_SPECIFIC_ATTRIBUTE_1_BITMASK 0b00001000
#define SPRITE_SPECIFIC_ATTRIBUTE_2_BITMASK 0b00000100
#define SUBPIXELS_PER_PIXEL 16

/*sprite ids*/
#define MUFFIN 1

#define SCREEN_SIZE_X 32
#define SCREEN_SIZE_Y 30
#define NUMBER_TO_TILE 48

/*remove before release*/
#define DEBUG

#define freeSprite(ptr) ptr->id = NO_SPRITE
#define getKey(bitmask) (controller1 & bitmask)
#define getKeyDown(bitmask) (controller1 & bitmask && !(controller1Prev & bitmask))
#define getKeyUp(bitmask) (controller1Prev & bitmask && !(controller1 & bitmask))
#define isNegativeChar(c) (c & 0b10000000)
#ifndef __CC65__ /*to avoid annoying vs code errors*/
	#define frameCountNoOverflow updateSprites
#endif

typedef struct {
	
	unsigned char positionX, positionY;
	signed char subPixelX, subPixelY; /*16 subPixels = 1 pixel*/
	signed char velocityX, velocityY; /*1 velocity = 1 subPixel per frame, can be negative, positive y is down*/
	unsigned char id;
	unsigned char oamAttributes; /*oam attributes, if bit 4 (unused) is set then the sprite has just been initialized, bits 5 and 6 (also unused) are sprite specific*/

	union { /*a sprite can have either two 8-bit attributes or one 16-bit attribute (or use both in different situations, I don't care!)*/

		struct { unsigned char attrib1, attrib2; };
		unsigned short longAttrib;
	};
} sprite; /*a sprite that gets updated every frame*/

typedef union {

	unsigned short seed;
	struct {

		unsigned char randByte1;
		unsigned char randByte2;
	};
} rngSeed;

#pragma bss-name(push, "ZEROPAGE")

unsigned char global_i, global_j; /*for use by loops, in the zeropage, make sure not to have conflicts!*/
unsigned char controller1, controller1Prev;
unsigned char frameCount;
unsigned char frameCountOverflow;

rngSeed rng;
sprite* currentSprite;

#pragma region Debug Functions
#ifdef DEBUG

void printNumber(unsigned char number, unsigned char x, unsigned char y) {

	unsigned char output = 0;
	unsigned char i_temp; //debug functions don't need to worry about memory usage
	for(i_temp = 0; i_temp < 2; ++i_temp) {
		
		if(number >= 100) {

			++output;
			number -= 100;
		}
	}

	one_vram_buffer(output + NUMBER_TO_TILE, NTADR_A(x, y));

	output = 0;
	for(i_temp = 0; i_temp < 10; ++i_temp) {

		if(number >= 10) {

			++output;
			number -= 10;
		}
	}

	one_vram_buffer(output + NUMBER_TO_TILE, NTADR_A(x + 1, y));
	one_vram_buffer(number + NUMBER_TO_TILE, NTADR_A(x + 2, y));
}

#endif
#pragma endregion

typedef struct {

	signed char offsetX, offsetY;
	unsigned char sizeX, sizeY;
} boundingBox;

const unsigned char palette1[] = {
	
	0x2C, 0x00, 0x10, 0x30,
	0x2C, 0x0F, 0x17, 0x39,
	0, 0, 0, 0,
	0, 0, 0, 0
};

#pragma region Sprites

typedef void (*spriteHandler)(void); /*to be called for each sprite every frame, responsible for initialization and update, must not use global_i*/

void updateMuffin(void) {
	
	if(currentSprite->oamAttributes & SPRITE_INITIALIZING_BITMASK) {

		currentSprite->oamAttributes &= ~SPRITE_INITIALIZING_BITMASK; /*no initialization required*/
	}
	
	currentSprite->velocityX = (controller1 & PAD_RIGHT) << 4;
	currentSprite->velocityX -= (controller1 & PAD_LEFT) << 3;

	currentSprite->velocityY = (controller1 & PAD_DOWN) << 2;
	currentSprite->velocityY -= (controller1 & PAD_UP) << 1;
}

sprite sprites[NUM_SPRITES];
unsigned char spriteShuffler[] = { 0, 1, 2, 3, 4, 5, 6, 7 };

const unsigned char muffinMetasprite[] = {

	-8, -16, 0x00, 1,
	-8, -8,  0x10, 1,
	 0, -8,  0x10, 1 | OAM_FLIP_H,
	 0, -16, 0x00, 1 | OAM_FLIP_H,
	128
};

const spriteHandler spriteHandlerJumpTable[] = {
	
	NULL, /*index 0*/
	updateMuffin /*index 1*/
};

const unsigned char* metaspriteDataPointers[] = {
	
	NULL, /*index 0*/
	muffinMetasprite /*index 1*/
};

/*uses global_i and global_j*/
void updateSprites(void) {

	register unsigned char xPosTemp, yPosTemp, idTemp;
	for(global_i = 0; global_i < sizeof(sprites) / sizeof(sprite); ++global_i) {
		
		currentSprite = &sprites[spriteShuffler[global_i]]; //shuffle sprites by changing their render priority each frame
		if(currentSprite->id == NO_SPRITE) continue; /*ignore if an empty sprite slot*/
		__asm__ ("sta %v", idTemp);
		
		/*update the position and subpixels for each sprite*/
		currentSprite->subPixelX += currentSprite->velocityX;
		currentSprite->positionX += currentSprite->subPixelX >> 4;
		__asm__ ("sta %v", xPosTemp); /*the x position is in the accumulator, store it for later in global_j*/
		currentSprite->subPixelX &= SUBPIXEL_BITMASK;
		
		currentSprite->subPixelY += currentSprite->velocityY;	
		currentSprite->positionY += currentSprite->subPixelY >> 4;
		__asm__ ("sta %v", yPosTemp); /*same as above*/
		currentSprite->subPixelY &= SUBPIXEL_BITMASK;
		
		spriteHandlerJumpTable[idTemp](); /*run the sprite id-specific code for the sprite*/
		oam_meta_spr(xPosTemp, yPosTemp, metaspriteDataPointers[idTemp]); /*draw the sprite*/
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
	
    currentSprite = sprites;
	for(global_j = 0; global_j < sizeof(sprites) / sizeof(sprite); ++global_j) {
		
		if(currentSprite->id == NO_SPRITE) {
			
			currentSprite->positionX = positionX;
			currentSprite->positionY = positionY;
			currentSprite->velocityX = 0;
			currentSprite->velocityY = 0;
			currentSprite->id = id;
			currentSprite->oamAttributes = oamAttributes;
			return /*currentSprite*/;
		}

        ++currentSprite;
	}
	
	currentSprite = NULL;
}

#pragma endregion

void updateRandom(void) {

	__asm__ ("lda %v", controller1);
	__asm__ ("clc");
	__asm__ ("adc %v", rng);
	__asm__ ("sta %v", rng);
	rng.seed ^= rng.seed << 7;
	rng.seed ^= rng.seed >> 9;
	rng.seed ^= rng.seed << 8;
}

void init(void) {

	controller1 = pad_poll(0);

	frameCount = 0;
	frameCountOverflow = FALSE;
	rng.seed = 0b111101011101000U + controller1;

	initializeSprite(MUFFIN, 0x40, 0x7F, SPRITE_INITIALIZING_BITMASK);
	controller1Prev = 0;
}

void main(void) {

    ppu_off();
    pal_bg(palette1);
    pal_spr(palette1);
	bank_bg(0);
	bank_spr(1);
	
	init();

    ppu_on_all();
	set_vram_buffer();

	controller1Prev = controller1;
    while(TRUE) {

        ppu_wait_nmi();
        
		controller1 = pad_poll(0);
		updateRandom();
		
        oam_clear();
        updateSprites();

		controller1Prev = controller1;
		
		__asm__ ("inc %v", frameCount);
		__asm__ ("bne %g", frameCountNoOverflow);
		frameCountOverflow = TRUE;
		continue;
		
		frameCountNoOverflow:
		frameCountOverflow = FALSE;
    }
}