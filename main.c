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

#define freeSprite(ptr) ptr->id = NO_SPRITE

typedef struct {
	
	unsigned char positionX, positionY;
	signed char subPixelX, subPixelY; /*16 subPixels = 1 pixel*/
	signed char velocityX, velocityY; /*1 velocity = 1 subPixel per frame, can be negative, positive y is down*/
	unsigned char id;
	unsigned char attributes; /*oam attributes, if bit 4 (normally unused) is set then the sprite has just been initialized*/
} sprite;

typedef struct {

    unsigned char highByte;
    unsigned char lowByte;
    unsigned char tileId;
    unsigned char eof;
} vram_single_update_buffer;

#pragma bss-name(push, "ZEROPAGE")

unsigned char global_i;
unsigned char global_j;
sprite* currentSprite;

const unsigned char test[] = "HI MUFFINS";

const unsigned char bgPalette[] = {
	
	BLACK, DARK_GREY, LIGHT_GREY, WHITE,
	0, 0, 0, 0,
	0, 0, 0, 0,
	0, 0, 0, 0
};

#pragma region sprites

typedef void (*spriteHandler)(void); /*to be called for each sprite every frame, responsible for initialization and update, must use global_j for loops*/

sprite sprites[NUM_SPRITES];

void updateStar(void) {
	
	if((currentSprite->attributes & SPRITE_INITIALIZING_BITMASK) > 0) currentSprite->attributes &= ~SPRITE_INITIALIZING_BITMASK;
	
	currentSprite->velocityY += 1;
	if(currentSprite->velocityY > 12) currentSprite->velocityY = 12;
}

const unsigned char starMetasprite[] = {
	
	0, 0, 0x01, 0,
	0, 8, 0x11, 0,
	8, 0, 0x01, 0 | OAM_FLIP_H,
	8, 8, 0x11, 0 | OAM_FLIP_H,
	128
};

const spriteHandler spriteHandlerJumpTable[] = {
	
	NULL,
	updateStar
};

const unsigned char* metaspriteDataPointers[] = {
	
	NULL,
	starMetasprite
};

/*uses global_i*/
void updateSprites(void) {
    
	currentSprite = sprites;
	unsigned char negativePixelsTemp;
	for(global_i = 0; global_i < sizeof(sprites) / sizeof(sprite); ++global_i) {
		
		if(currentSprite->id == NO_SPRITE) continue; /*ignore if an empty sprite slot*/
		
		/*update the position and subPixels for each sprite*/
		currentSprite->subPixelX += currentSprite->velocityX;
		if(currentSprite->subPixelX >= (signed char)0) {
			
			currentSprite->positionX += currentSprite->subPixelX >> 4;
			currentSprite->subPixelX &= SUBPIXEL_BITMASK;
		} else {
			
			negativePixelsTemp = -currentSprite->subPixelX >> 4;
			currentSprite->positionX -= negativePixelsTemp;
			currentSprite->subPixelX = -(negativePixelsTemp & SUBPIXEL_BITMASK);
		}
		
		currentSprite->subPixelY += currentSprite->velocityY;
		if(currentSprite->subPixelY >= (signed char)0) {
			
			currentSprite->positionY += currentSprite->subPixelY >> 4;
			currentSprite->subPixelY &= SUBPIXEL_BITMASK;
		} else {
			
			negativePixelsTemp = -currentSprite->subPixelY >> 4;
			currentSprite->positionY -= negativePixelsTemp;
			currentSprite->subPixelY = -(negativePixelsTemp & SUBPIXEL_BITMASK);
		}
		
		spriteHandlerJumpTable[currentSprite->id](); /*run the sprite id-specific code for the sprite*/
		oam_meta_spr(currentSprite->positionX, currentSprite->positionY, metaspriteDataPointers[currentSprite->id]); /*draw the sprite*/
		
		++currentSprite;
	}
}

/*uses global_j*/
void initializeSprite(unsigned char id, unsigned char positionX, unsigned char positionY, unsigned char attributes) {
	
    currentSprite = sprites;
	for(global_j = 0; global_j < sizeof(sprites) / sizeof(sprite); ++global_j) {
		
		if(currentSprite->id == NO_SPRITE) {
			
			currentSprite->positionX = positionX;
			currentSprite->positionY = positionY;
			currentSprite->velocityX = 0;
			currentSprite->velocityY = 0;
			currentSprite->id = id;
			currentSprite->attributes = attributes | SPRITE_INITIALIZING_BITMASK; //set the sprite to be initializing
		}

        ++currentSprite;
	}
	
	currentSprite = NULL;
}

#pragma endregion

void main(void) {

    ppu_off();
    pal_bg(bgPalette);
	pal_spr(bgPalette);
	bank_spr(1);

    vram_adr(NTADR_A(7, 14));
	vram_write(test, sizeof(test));

    global_i = 0;
    while(test[global_i]) {

        vram_put(test[global_i]);
        ++global_i;
    }

    ppu_on_all();
    initializeSprite(STAR, 0x40, 0x88, 0);

    while(TRUE) {
        
        ppu_wait_nmi();
        oam_clear();
        updateSprites();
    }
}
