#include "lib/neslib.h"
#include "lib/nesdoug.h"
#include "sprites.h"
#include "debug.h"
#include "asm.h"

#define freeSprite(index) spriteIds[index] = NO_SPRITE
#define getKey(bitmask) (controller1 & bitmask)
#define getKeyDown(bitmask) (controller1 & bitmask && !(controller1Prev & bitmask))
#define getKeyUp(bitmask) (controller1Prev & bitmask && !(controller1 & bitmask))
#define isNegativeChar(c) (c & 0b10000000)
#ifndef __CC65__ /*to avoid annoying vs code errors*/
	#define frameCountNoOverflow updateSprites
#endif

const unsigned char palette1[] = {
	
	0x2C, 0x00, 0x10, 0x30,
	0x2C, 0x0F, 0x17, 0x39,
	0, 0, 0, 0,
	0, 0, 0, 0
};

void updateRandom(void) {

	/*xorshift rng with some added randomness from the controller 1 inputs*/
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
	rng.seed = 0b111101011101000U + controller1; /*add a bit of randomness based on the inputs on controller 1*/
	
	/*initialize sprite zero*/
	spriteIds[spriteZero] = SPRITE_ZERO;
	spriteVelocitiesX[spriteZero] = 0;
	spriteVelocitiesY[spriteZero] = 0;
	spritePositionsX[spriteZero] = 0;
	spritePositionsY[spriteZero] = 0;
	spriteOamAttributes[spriteZero] = 0; /*don't ever initialize*/

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