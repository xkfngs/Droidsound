/******************************************************************************\
* Authors:  Iconoclast                                                         *
* Release:  2013.11.26                                                         *
* License:  CC0 Public Domain Dedication                                       *
*                                                                              *
* To the extent possible under law, the author(s) have dedicated all copyright *
* and related and neighboring rights to this software to the public domain     *
* worldwide. This software is distributed without any warranty.                *
*                                                                              *
* You should have received a copy of the CC0 Public Domain Dedication along    *
* with this software.                                                          *
* If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.             *
\******************************************************************************/
#include "vu.h"

static void VMACQ(usf_state_t * state, int vd, int vs, int vt, int e)
{
    (void)state;
    vd &= vs &= vt &= e &= 0; /* unused */
    if (vd != vs || vt != e)
        return;
    message(state, "VMACQ\nUnimplemented.", 3); /* untested, any N64 ROMs use this?? */
    return;
}
