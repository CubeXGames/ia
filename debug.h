#ifndef DEBUG_H
#define DEBUG_H

#include "globalDefs.h"

/*remove before release*/
#define DEBUG

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

#endif