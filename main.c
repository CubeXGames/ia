#include "lib/neslib.h"
#include "lib/nesdoug.h"
#include "sprites.h"
#include "asm.h"

#define TRUE 1
#define FALSE 0

#define BLACK 0x0f
#define DARK_GREY 0x00
#define LIGHT_GREY 0x10
#define WHITE 0x30

#define SCREEN_SIZE_X 32
#define SCREEN_SIZE_Y 30
#define NUMBER_TO_TILE 48

/*remove before release*/
#define DEBUG

#define freeSprite(index) spriteIds[index] = NO_SPRITE
#define getKey(bitmask) (controller1 & bitmask)
#define getKeyDown(bitmask) (controller1 & bitmask && !(controller1Prev & bitmask))
#define getKeyUp(bitmask) (controller1Prev & bitmask && !(controller1 & bitmask))
#define isNegativeChar(c) (c & 0b10000000)
#ifndef __CC65__ /*to avoid annoying vs code errors*/
	#define frameCountNoOverflow updateSprites
#endif

typedef union {

	unsigned short seed;
	struct {

		unsigned char randByte1;
		unsigned char randByte2;
	};
} rngSeed; /*allows setting the seed and accessing both bytes easily*/

#pragma bss-name(push, "ZEROPAGE")

unsigned char controller1, controller1Prev;
unsigned char frameCount;
unsigned char frameCountOverflow;

rngSeed rng;

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

const unsigned char palette1[] = {
	
	0x2C, 0x00, 0x10, 0x30,
	0x2C, 0x0F, 0x17, 0x39,
	0, 0, 0, 0,
	0, 0, 0, 0
};

void updateRandom(void) {

	/*xorshift rng with some added randomness of the controller 1 inputs*/
	__asm__ ("lda %v", controller1);
	__asm__ ("clc");
	__asm__ ("adc %v", rng);
	__asm__ ("sta %v", rng);
	rng.seed ^= rng.seed << 7;
	rng.seed ^= rng.seed >> 9;
	rng.seed ^= rng.seed << 8;
}

void init(void) {
	
    pal_bg(palette1);
    pal_spr(palette1);
	bank_bg(0);
	bank_spr(1);

	controller1 = pad_poll(0);

	frameCount = 0;
	frameCountOverflow = FALSE;
	rng.seed = 0b111101011101000U + controller1; /*add a bit of randomness based on the inputs on the controller*/
	
	/*initialize player*/
	spriteIds[player] = PLAYER;
	spriteVelocitiesX[player] = 0;
	spriteVelocitiesY[player] = 0;
	spritePositionsX[player] = 0x40;
	spritePositionsY[player] = 0x7F;
	spriteOamAttributes[player] = SPRITE_INITIALIZING_BITMASK;
}

void main(void) {

    ppu_off();
	
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