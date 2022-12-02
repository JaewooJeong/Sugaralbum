#ifndef SHOTDETECTION_H
#define SHOTDETECTION_H
#include <iostream>
#include <vector>
#include <../armadillo/armadillo>
#include "MASP.h"


using namespace std;
using namespace arma;

/*
    ShotDetection.h
    ver. 1.56

		Automatically find photo slide shot change location
		based on music beat and chord transition.

    MARG 2015/07/01
*/

vector<double> find_shot_change_loc(short* x_in, int x_sz);

void audio2mel(short* x, int x_sz, int fs, int win_length, int hop_size, mat& mel, vec& fi); // Jaehun
void audio2mel_old(vec x, int fs, int win_length, int hop_size, mat& mel, vec& fi);
void audio2spec(vec x, int fs, int win_length, int hop_size, cx_mat& spec, vec& fi); // yeonhwa
mat spec2melspec(cx_mat spec, vec fi,int fs); // yeonhwa
rowvec onset_detection(mat melspec);  // harim
mat bpm_estimation(mat in_onset,double in_len_x,double in_fs, int hopsize, double* bpm); // il-young
rowvec indexing(rowvec beat_grid,int in_spec_size_2); // yoonchang
uword db_detection(vec grid_idx, mat melspec); // yoonchang
mat cos_sim_mtx(mat A, mat B); // yoonchang
double cos_sim(vec A, vec B); // yoonchang
double Vmag(vec A); // yoonchang
vec db_grid(int spec_size_2, vec grid_idx, int ie);
mat bpmgrids(int fs, int hopsize, int len_x);


#endif // SHOTDETECTION_H
