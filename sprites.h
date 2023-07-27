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

typedef void (*spriteHandler)(void); /*a handler for sprites, must not use global_i*/

const unsigned char spriteZero = 0;
const unsigned char player = 1;

const unsigned char playerMetasprite[] = {

	-8, -16, 0x00, 1,
	-8, -8,  0x10, 1,
	 0, -8,  0x10, 1 | OAM_FLIP_H,
	 0, -16, 0x00, 1 | OAM_FLIP_H,
	128
};

#pragma bss-name(push, "ZEROPAGE")

unsigned char global_i, global_j; /*for use by loops, in the zeropage, make sure not to have conflicts!*/
unsigned char currentSprite;
unsigned char spriteShuffler[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };

#pragma bss-name(pop)

typedef struct {

	signed char offsetX, offsetY;
	unsigned char sizeX, sizeY;
} boundingBox;

typedef union {
	
	struct { unsigned char attrib1, attrib2; }
	unsigned short longAttrib;
} spriteAttribute;

unsigned char spritePositionsX[NUM_SPRITES];
unsigned char spritePositionsY[NUM_SPRITES];
signed char spriteSubPixelsX[NUM_SPRITES];
signed char spriteSubPixelsY[NUM_SPRITES];
signed char spriteVelocitiesX[NUM_SPRITES];
signed char spriteVelocitiesY[NUM_SPRITES];
unsigned char spriteIds[NUM_SPRITES];
unsigned char spriteOamAttributes[NUM_SPRITES];
spriteAttribute spriteAttributes[NUM_SPRITES];

void noSpriteUpdate(void) {}

void updatePlayer(void) {
	
	if(spriteOamAttributes[currentSprite] & SPRITE_INITIALIZING_BITMASK) {

		spriteOamAttributes[currentSprite] &= ~SPRITE_INITIALIZING_BITMASK; /*no initialization required*/
	}
	
	spriteVelocitiesX[currentSprite] = (controller1 & PAD_RIGHT) << 4;
	spriteVelocitiesX[currentSprite] -= (controller1 & PAD_LEFT) << 3;

	spriteVelocitiesY[currentSprite] = (controller1 & PAD_DOWN) << 2;
	spriteVelocitiesY[currentSprite] -= (controller1 & PAD_UP) << 1;
}

/*uses global_i and global_j*/
void updateSprites(void) {

	for(global_i = 0; global_i < NUM_SPRITES; ++global_i) {
		
		currentSprite = spriteShuffler[global_i];
		if(spriteIds[currentSprite] == NO_SPRITE) continue; /*ignore if an empty sprite slot*/
		
		/*update the position and subpixels for each sprite*/
		/*you might think that moving in a negative direction would require extra code when updating the sprite position, but it surprisingly doesn't!*/
		spriteSubPixelsX[currentSprite] += spriteVelocitiesX[currentSprite];
		spritePositionsX[currentSprite] += spriteSubPixelsX[currentSprite] >> 4;
		spriteSubPixelsX[currentSprite] &= SUBPIXEL_BITMASK;
		
		spriteSubPixelsY[currentSprite] += spriteVelocitiesY[currentSprite];
		spritePositionsY[currentSprite] += spriteSubPixelsY[currentSprite] >> 4;
		spriteSubPixelsY[currentSprite] &= SUBPIXEL_BITMASK;
		
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
	
    currentSprite = sprites;
	for(global_j = 0; global_j < sizeof(sprites) / sizeof(sprite); ++global_j) {
		
		if(currentSprite->id == NO_SPRITE) {
			
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

const spriteHandler spriteHandlerJumpTable[] = {
	
	noSpriteUpdate, /*index 0*/
	updatePlayer, /*index 1*/
	noSpriteUpdate /*index 2*/
};

const unsigned char* metaspriteDataPointers[] = {
	
	NULL, /*index 0*/
	playerMetasprite /*index 1*/
};