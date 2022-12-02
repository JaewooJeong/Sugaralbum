#ifndef MASP_H
#define MASP_H
#include "../lib/cq/CQSpectrogram.h"
#include "../lib/cq/CQParameters.h"
#include <vector>
#include <../armadillo/armadillo>
//#include <sndfile.hh>
#include <math.h>
#include "../lib/src/dsp/FFT.h"
#include <time.h>
//#include <fftw3.h>
#include "../lib/fftw/fftw.h"
#include "KernelMatrices.hpp"
// #include <unistd.h>
// #include <ios>
// #include <fstream>
// #include <iostream>

using namespace std;
using namespace arma;

/*
    MASP.h (MARG Audio Signal Processing)
    ver. alpha 0.06
        This header includes some useful functions for audio signal processing
        If you want to add any function (method) related to this criteria,
        Please add approprieate comment with it.

        TODO :
            1. Complete beat tracking algorithm
            2. Add subfunctions for it
            3. Add miscellaneous functions (i.e. more window functions)
            4. Test ifft / istft function
            5. Dynamic chroma function??

    JaehunKim 2015/05/31

    **EDIT**
    process_mem_usage() : added from StackOverflow answer 15/0820
    melspec_short() : added for memory optimization 15/08/18
    audioread_short() :added for test :: JaehunKim 15/08/18
    audioread() : memory leak fix
    fft_w2()    : memory leak fix? (potential)
    melspec()   : added mel-spectrogram :: JaehunKim 15/08/04
    fft_w2()    : added for LG Uplus request :: JaehunKim 15/08/04
    fft_w3()    : function name change :: JaehunKim 15/08/04
    fft_jh()    : memory leak fix :: JaehunKim 15/07/xx
*/

// High level functions

// Utilities
// void audioread(string fn, vec& x, int* fs);
// void audioread_short(string fn, short** x, int* fs, int* lenx);
vec short2vec(short* x, int sz);
vec gen_mel_kern(int nfft, int fs);
// void process_mem_usage(double& vm_usage, double& resident_set);

// ETC
vec flux(mat X);
vec simplePeakFind(double environment, vec data, double thresh);

// Chroma
mat wav2chroma_iy(vec x, int fs);

// Window functions
vec hamming(int len);

// FFT / STFT functions
cx_mat melspec(vec x, int nfft, vec window, int noverlap, int fs);
cx_mat melspec_short(short* x, int lenx, int nfft, vec window, int noverlap, int fs);
cx_mat stft(vec x, int nfft, vec window, int noverlap, int fs);
//cx_mat stft_half(vec x, int nfft, vec window, int noverlap, int fs); // for fftw3
vec istft(cx_mat X, vec window, int noverlap);
cx_vec fft_jh(vec x, int nfft);
vec ifft_jh(const cx_vec x, int nfft);
//cx_vec fft_w3(vec x, int nfft); // for fftw3
cx_vec fft_w2(vec x, int nfft);

// Visualization
void plot(vec x, string fn);
void imshow(mat X, string fn);

// Miscellaneous

#endif // MASP_H
