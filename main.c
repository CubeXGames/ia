#include "lib/neslib.h"
#include "lib/nesdoug.h"

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
#define SUBPIXELS_PER_PIXEL 16

/*sprite ids*/
#define STAR 1

#define SCREEN_SIZE_X 32
#define SCREEN_SIZE_Y 30

#define freeSprite(index) sprites[index].id = NO_SPRITE

#pragma bss-name(push, "ZEROPAGE")

const unsigned char test[] = "test thingy yay";

const unsigned char bgPalette[] = {
	
	BLACK, DARK_GREY, LIGHT_GREY, WHITE,
	0, 0, 0, 0,
	0, 0, 0, 0,
	0, 0, 0, 0
};

const unsigned char starMetasprite[] = {
	
	0, 0, 0x01, 0,
	0, 8, 0x11, 0,
	8, 0, 0x01, 0 | OAM_FLIP_H,
	8, 8, 0x11, 0 | OAM_FLIP_H,
	128
};

typedef struct {
	
	unsigned char positionX, positionY;
	signed char subPixelX, subPixelY; /*16 subPixels = 1 pixel*/
	signed char velocityX, velocityY; /*1 velocity = 1 subPixel per frame, can be negative, positive y is down*/
	unsigned char id;
	unsigned char attributes; /*oam attributes, if bit 4 is set then the sprite has just been initialized*/
} sprite;

typedef void (*spriteHandler)(sprite*); /*function pointers in C are way too confusing*/

void updateStar(sprite* sprite) {
	
	if((sprite->attributes & SPRITE_INITIALIZING_BITMASK) > 0) sprite->attributes &= ~SPRITE_INITIALIZING_BITMASK;
	
	sprite->velocityY += 1;
	if(sprite->velocityY > 12) sprite->velocityY = 12;
}

const spriteHandler spriteHandlerJumpTable[] = {
	
	NULL,
	updateStar
};

const unsigned char* metaspriteDataPointers[] = {
	
	NULL,
	starMetasprite
};

sprite sprites[NUM_SPRITES];
unsigned char global_i, global_j;

void updateSprites(void) {
	
	register sprite* spritePtr = sprites;
	unsigned char negativePixelsTemp;
	for(global_i = 0; global_i < sizeof(sprites) / sizeof(sprite); ++global_i) {
		
		if(spritePtr->id == NO_SPRITE) continue; /*ignore if an empty sprite slot*/
		
		/*update the position and subPixels for each sprite*/
		spritePtr->subPixelX += spritePtr->velocityX;
		if(spritePtr->subPixelX >= (signed char)0) {
			
			spritePtr->positionX += spritePtr->subPixelX >> 4;
			spritePtr->subPixelX &= SUBPIXEL_BITMASK;
		} else {
			
			negativePixelsTemp = -spritePtr->subPixelX >> 4;
			spritePtr->positionX -= negativePixelsTemp;
			spritePtr->subPixelX = -(negativePixelsTemp & SUBPIXEL_BITMASK);
		}
		
		spritePtr->subPixelY += spritePtr->velocityY;
		if(spritePtr->subPixelY >= (signed char)0) {
			
			spritePtr->positionY += spritePtr->subPixelY >> 4;
			spritePtr->subPixelY &= SUBPIXEL_BITMASK;
		} else {
			
			negativePixelsTemp = -spritePtr->subPixelY >> 4;
			spritePtr->positionY -= negativePixelsTemp;
			spritePtr->subPixelY = -(negativePixelsTemp & SUBPIXEL_BITMASK);
		}
		
		spriteHandlerJumpTable[spritePtr->id](spritePtr); /*run the sprite id-specific code for the sprite*/
		oam_meta_spr(spritePtr->positionX, spritePtr->positionY, metaspriteDataPointers[spritePtr->id]); /*draw the sprite*/
		
		++spritePtr;
	}
}

unsigned char initializeSprite(unsigned char id, unsigned char positionX, unsigned char positionY, unsigned char attributes) {
	
	for(global_j = 0; global_j < sizeof(sprites) / sizeof(sprite); ++global_j) {
		
		if(sprites[global_j].id == NO_SPRITE) {
			
			sprites[global_j].positionX = positionX;
			sprites[global_j].positionY = positionY;
			sprites[global_j].velocityX = 0;
			sprites[global_j].velocityY = 0;
			sprites[global_j].id = id;
			sprites[global_j].attributes = attributes | SPRITE_INITIALIZING_BITMASK;
			
			return global_j; /*global_j is the index of the sprite*/
		}
	}
	
	return -1;
}

void freeSprite(unsigned char index) {
	
	sprites[index].id = NO_SPRITE;
}

void main(void) {
	
	ppu_off();
	pal_bg(bgPalette);
	pal_spr(bgPalette);
	bank_spr(1);
	
	vram_adr(NTADR_A(7, 14));
	vram_write(test, sizeof(test));
	
	ppu_on_all();
	
	initializeSprite(STAR, 0x40, 0x88, 0);
	
	while(TRUE) {
		
		ppu_wait_nmi();
		
		oam_clear();
		updateSprites();
	}
}
