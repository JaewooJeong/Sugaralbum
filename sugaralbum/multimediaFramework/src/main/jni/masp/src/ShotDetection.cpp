#include "../include/ShotDetection.h"
#include "../include/KernelMatrices.hpp"
#define MAXBPM 150
#define MINBPM 60

vector<double> find_shot_change_loc(short* x_in, int x_sz)
{
    // function variable
    int win_length = 1024;
    int hop_size = 512;
//    vec x(x_in, x_sz,false);
    int fs = 22050; // will be changed if more optimization needed
    vec fi;
    mat mel_spec;

    // get spectrogram
//    audio2spec(x,fs,win_length,hop_size,spec,fi);
    // get mel-spectrogram
//    mel_spec = spec2melspec(spec,fi,fs);

    // get mel from PCM and set n_frames
    audio2mel(x_in,x_sz,fs,win_length,hop_size,mel_spec,fi);
    int n_frames = mel_spec.n_cols;

    // free input PCM data (it might be dangerous operation!)
    // delete [] x_in;

    // calc onset
    rowvec onset = onset_detection(mel_spec);

    // estimate bpm and generate half-beat grid
    mat in_onset = onset;
    double bpm;

    mat beat_grid = bpm_estimation(in_onset,(double)x_sz,(double)fs, hop_size, &bpm);
    rowvec grid_idx = indexing(beat_grid, n_frames);

    // downbeat detection
    //(including similarity matrix from beat-wise sum mel spec. and edge detection)
    uword ie = db_detection(grid_idx.t(), mel_spec);
    vec blip_grid = db_grid(n_frames, grid_idx.t(), (int)ie);

    // free melspec (for memory optimization)
    mel_spec.clear();

    // just for print to check the result
    vec colindex = 1 + linspace(0,(n_frames-1)*(win_length-hop_size),n_frames);
    vec ti = ((colindex-1) + ((win_length)/2.)) / fs;
//    vec ti = linspace(0,x_in.size()/(float)fs,n_frames);

    vec time_stamp_vec = ti(find(blip_grid==1));
    vector<double> time_stamp(time_stamp_vec.begin(),time_stamp_vec.end());

    return time_stamp;
}

///////////////////////////////  JAEHUN KIM ////////////////////////////////////////////
void audio2mel(short* x, int x_sz, int fs, int win_length, int hop_size, mat& mel, vec& fi) {
    /* audio2mel short version
            compute mel-spectrogram which is calculated based on stft function from MASP.
        yeonhwakim 2015/08/18
    */
    // declare variables
    int noverlap = win_length - hop_size;
    int nfft = win_length;
    vec window = hamming(win_length);
    vec _fi(nfft/2+1,1);

    cx_mat cx_melspec = melspec_short(x,x_sz,nfft,window,noverlap,fs);

    // mel to dB
    mat ms_abs = arma::abs(cx_melspec);
    mat melspec_out = 20*log10(ms_abs+datum::eps);
    mel = max(melspec_out,ones<mat>(melspec_out.n_rows,melspec_out.n_cols)*(max(max(melspec_out))-60));

    double unit = fs/(float)nfft;
    for (int i=0; i<nfft/2+1; ++i) _fi(i) = unit*i;
    fi = _fi;
}

void audio2mel_old(vec x, int fs, int win_length, int hop_size, mat& mel, vec& fi) {
    /* audio2mel
            compute mel-spectrogram which is calculated based on stft function from MASP.
        yeonhwakim 2015/08/04
    */
    // declare variables
    int noverlap = win_length - hop_size;
    int nfft = win_length;
    vec window = hamming(win_length);
    vec _fi(nfft/2+1,1);

    cx_mat cx_melspec = melspec(x,nfft,window,noverlap,fs);

    // mel to dB
    mat ms_abs = arma::abs(cx_melspec);
    mat melspec_out = 20*log10(ms_abs+datum::eps);
    mel = max(melspec_out,ones<mat>(melspec_out.n_rows,melspec_out.n_cols)*(max(max(melspec_out))-60));

    double unit = fs/(float)nfft;
    for (int i=0; i<nfft/2+1; ++i) _fi(i) = unit*i;
    fi = _fi;
}

///////////////////////////////  YEON HWA KIM ////////////////////////////////////////////

void audio2spec(vec x, int fs, int win_length, int hop_size, cx_mat& spec, vec& fi)
{
    /* audio2spec
            compute spectrogram which is calculated based on stft function from MASP.
        yeonhwakim 2015/06/24
    */

    // declare variables
    int noverlap = win_length - hop_size;
    int nfft = win_length;
    vec window = hamming(win_length);
    //int nx = x.n_elem; // length of signal x
    vec _fi(nfft+1,1);

    // compute spectrogram
    spec = stft(x, nfft, window, noverlap, fs);

    double unit = fs/(float)nfft;
    for (int i=0; i<nfft/2+1; ++i) _fi(i) = unit*i;
    fi = _fi;

}

mat spec2melspec(cx_mat spec, vec fi,int fs)
{
    /* spec2melspec
            convert spectrogram to mel-spec. 40 band.
        yeonhwakim 2015/06/24
    */

    // calculates fi2mel data
    // fi is converted to the fi2mel, 40 band mel scaled data.
    vec fi2mel =gen_mel_kern((spec.n_rows-1)*2,fs);
    int length_ti = spec.n_cols;  // we don't use ti anymore since it is same with spec.n_cols (ychan)
    int length_fi = spec.n_rows;

    cx_mat mel_spec = zeros<cx_mat>(40, length_ti); // generate an empty matrix for mel-scale spectrogram
    mat melspec_out;
    for(int i=0; i < length_ti; i++)
    {
        for(int j=0; j < length_fi; j++)
        {
            mel_spec((int)fi2mel(j)-1,i) += spec(j,i);
        }
    }

    // mel to dB
    mat ms_abs = arma::abs(mel_spec);
    melspec_out = 20*log10(ms_abs+datum::eps);
    melspec_out = max(melspec_out,ones<mat>(melspec_out.n_rows,melspec_out.n_cols)*(max(max(melspec_out))-60));

    return melspec_out;
}


////////////////////////////////////////// IL-YOUNG JEONG /////////////////////////////////////////

mat bpm_estimation(mat in_onset,double in_len_x,double in_fs, int hopsize, double* bpm)
{
    /* bpm_estimation
            estimate bpm
        Il-Young Jeong 2015/06/24
        Jaehun Kim 2015/07/14 (modified)
    */

    mat bpm_template = bpmgrids(in_fs,hopsize,in_len_x);
//    mat bpm_template(150,30106);
//    for (int i=0; i<150; ++i) {
//        for (int j=0; j<30106; ++j) {
//            bpm_template(i,j) = bpm_template_array[i][j];
//        }
//    }

    mat beat_hop(beat_hop_array,MAXBPM,1);

    double beat_interval;
    mat num_beats = zeros<mat>(1,MAXBPM);
    mat temp_grid;
    double i_end;
    mat result;
    double pkx;
    uvec idx;
    mat val = zeros<mat>(1,MAXBPM);
    mat val_idx = zeros<mat>(1,MAXBPM);
    double maxx;
    uvec idxx;
    double max_idx;
    mat beat_grid;

    // Grid template
    for (int bpm_i=MINBPM; bpm_i<=MAXBPM; bpm_i++)
    {
        beat_interval = MINBPM/(double)bpm_i*in_fs;
        num_beats(bpm_i-1) = ceil(in_len_x/beat_interval);
    }



    // Max area detection
    for (int bpm_i=MINBPM; bpm_i<=MAXBPM; bpm_i++)
    {
        result = zeros<mat>(1,beat_hop(bpm_i-1));
        for (int i=0; i<beat_hop(bpm_i-1); i++)
        {
            i_end = i+in_onset.size()-1;
            temp_grid = bpm_template(span(bpm_i-1,bpm_i-1),span(i,i_end));
            result(i) = accu(temp_grid%in_onset);
        }
        pkx = max(max(result));
        idx =find(result==pkx,1,"first");
        val(bpm_i-1) = result(0,idx(0))/num_beats(bpm_i-1);
        val_idx(bpm_i-1) = idx(0);
    }

    //output
    maxx = max(max(val));
    idxx = find(val==maxx,1,"first");
    *bpm = (int)idxx(0)+1;
    max_idx = val_idx(*bpm-1);
    beat_grid = bpm_template(span(*bpm-1,*bpm-1),span(max_idx,max_idx+in_onset.size())); //length(onset)=length(beat_grid)-1
    beat_grid(find(beat_grid<1)).zeros();


    return beat_grid;
}

/////////////////////////////////// HARIM KANG ////////////////////////////////////////

rowvec onset_detection(mat melspec)
{
    /* onset detection

    input
    melspec : mel frequency spectrogram

    output
    onset vector
    */

// half-wave recified 1st order difference
    rowvec mm;
    mat diff;

// matlab diff function
    /*
    DIFF(X), for a vector X, is [X(2)-X(1)  X(3)-X(2) ... X(n)-X(n-1)].
        DIFF(X), for a matrix X, is the matrix of row differences, [X(2:n,:) - X(1:n-1,:)]
    */
    melspec = melspec.t();
    //diff = (melspec.rows(1,melspec.n_rows)-melspec.rows(0,melspec.n_rows-1)).t();
    diff = melspec.rows(1,melspec.n_rows-1)-melspec.rows(0,melspec.n_rows-2);
    diff = diff.t();

    mat non_mm;
    diff.elem( find(diff<0)).zeros();

    mm = mean(diff);

    // # of cols
    int ncol = mm.n_cols;
// dc-removed onset waveform
//y=onset = filter([1 -1], [1 -.99], mm);
// A = [1 -.99], B = [1 -1]
// a(1)*y(n) = b(1)*x(n) + b(2)*x(n-1) - a(2)*y(n-1)
// y(n) = x(n) - x(n-1) +0.99*y(n-1)

    rowvec prev_on = zeros<rowvec>(ncol);
    prev_on(0) = mm(0);
    for(int i=1; i<ncol; i++)
    {
        prev_on(i) = mm(i) - mm(i-1) + 0.99*prev_on(i-1);
    }

    prev_on.elem(find(prev_on<0)).zeros();

    return prev_on;

}


rowvec indexing(rowvec beat_grid,int in_spec_size_2)
{
    // matlab indexing function
    // function grid_idx = indexing(beat_grid, spec)
    /*
        half beat grid indexing

        input
        beat_grid : beat grid
        spec : spectrogram

        output
        grid_idx : grid index
    */
    // matlab indexing result -1 is used as output

    // return a column vector(uvec) containing the indices of elements of beat_grid that are non-zero

    uvec grid_idx_1 = find(beat_grid>0)+1 ;

    // conversion from uvec to rowvec
    rowvec grid_idx = conv_to<rowvec>::from(grid_idx_1);

    // length of original index
    int ncolg = grid_idx.n_cols;

    // generate new grid vector
    rowvec grid_idx2 = zeros<rowvec>(2*ncolg+3);
    rowvec temp_idx = zeros<rowvec>(ncolg+1);

    for(int i=0; i<ncolg; i++)
    {
        temp_idx(i+1)=grid_idx(i);
    }

    // put values in new grid vector (0~2n-1)
    for(int i=1; i<temp_idx.n_cols; i++)
    {
        grid_idx2(2*i)=temp_idx(i);
    }

    grid_idx2(1)=round(temp_idx(1)/2);

    for(int i=1; i<temp_idx.n_cols-1; i++)
    {
        grid_idx2(2*i+1)=round((temp_idx(i)+temp_idx(i+1))/2);
    }

    grid_idx.clear();
    temp_idx.clear();

    grid_idx = grid_idx2.cols(1,grid_idx2.n_cols-1)-1;

    // column size of spec
    int ncol = in_spec_size_2;
    vec grid_idx3 = grid_idx.elem(find(grid_idx<ncol&&grid_idx>=0));

    return grid_idx3.t();
}

///////////////////////////// Yoon Chang Han //////////////////////////////////////

uword db_detection(vec grid_idx, mat melspec)
{
    /* db_detection
      This function finds downbeat position using self-similarity function.
        ~input~
          grid_idx : half-beat grid index
          melspec : mel frequency spectrogram
        ~output~
          ie : index of the first down beat
      Yoonchang Han 2015/06/26
    */

    // variables
    vec zero = zeros<vec>(1);
    vec sum_idx;
    mat melsum = zeros<mat>(40,grid_idx.n_rows);
    mat ker(16,16);
    const int N = 16;
    mat corcoeff;
    vec vote_eight = zeros<vec>(8);
    uword ie;

    // external variables
    //string ker_name = "./data/vars/ker.csv";
    //ker.load(ker_name,csv_ascii);
    for (int i=0; i<16; ++i)
    {
        for (int j=0; j<16; ++j)
        {
            ker(i,j) = ker_array[i][j];
        }
    }

    // sum mel spectrum beat-wise
    sum_idx = join_cols(zero, grid_idx);
    // sum_idx = sum_idx -1;

    for (int i=0; i<=sum_idx.n_rows-2; i++)          // subtract 2 here to get up to previous frame
    {
        for (int j=0; j<=39; j++)                      // of the end grid position
        {
            for (int k=sum_idx(i); k<=(sum_idx(i+1)-1); k++)
            {
                melsum(j,i) = melsum(j,i) + melspec(j,k);
            }
        }
    }

    // get similarity matrix(SM)
    mat SM = cos_sim_mtx(melsum,melsum);

    // free melsum (for memory optimizaation)
    melsum.clear();

    // zero padding around similarity matrix (SM)
    mat SM_z = zeros(SM.n_rows+N,SM.n_rows+N);


    // copy original SM into zero-padded one
    int SM_z_end = SM_z.n_rows-1;
    SM_z(span(N/2,SM_z_end-N/2),span(N/2,SM_z_end-N/2)) = SM;

    // free SM (for memory optimization
    SM.clear();

    SM_z.save("data/SM_z15.csv",csv_ascii);

    // store correlation coefficient on ns_ker
    vec ns_ker = zeros<vec>(SM_z_end-N+1);
    for (int i=0; i<=SM_z_end-N; i++)
    {
        corcoeff = cor(vectorise(ker),vectorise(SM_z(span(i,i+N-1),span(i,i+N-1))));
        ns_ker(i) = corcoeff(0,0);
    }

    // free SM_z (for memory optimization)
    SM_z.clear();

    // normalise
    vec ns_ker_n = ns_ker/max(ns_ker);

    // vote for eight half-bars to find downbeat among beat candidates
    vec ns_i;  // peak index
    ns_i = simplePeakFind(0, ns_ker_n, 0);  //(minpeak distance, data, theshold)

    vec ns_p = zeros<vec>(ns_i.n_rows);  // peak value
    for (int i=0; i<=ns_i.n_rows-1; i++)
    {
        ns_p(i) = ns_ker_n(ns_i(i));
    }

    // get remainder
    vec ns_rem = zeros<vec>(ns_i.n_rows);
    for (int i=0; i<=ns_i.n_rows-1; i++)
    {
        ns_rem(i) = ns_i(i) - (floor(ns_i(i)/8))*8;
    }

    // add peak values to beat candidates to find the most probable downbeat
    for (int i=0; i<=ns_rem.n_rows-1; i++)
    {
        vote_eight(ns_rem(i)) = vote_eight(ns_rem(i))+ ns_p(i);
    }

    // maximum value should be a downbeat position we are looking for!!
    double max_val = vote_eight.max(ie);  // ignore max_val as we only use the index "ie"
    ie = ie+7;  // just to avoid error in original matlab code... (to be fixed)

    // return value
    return ie;
}


mat cos_sim_mtx(mat A, mat B)
{
    /* cos_sim_mtx
      This function calculates similarity matrix between input matrices.
      Note that this function was only tested with self-similarity matrix (for now).
      Precisely, it is inverse of cosine similarity (i.e. 1-similarity).
        ~input~
          matrix you want to see similarity.
        ~output~
          similarity matrix.
      Yoonchang Han 2015/06/26
    */
    A = normalise(A);
    B = normalise(B);

    mat SM = zeros<mat>(A.n_cols,B.n_cols);
    for (int i=0; i<A.n_cols; i++)
    {
        for (int j=0; j<B.n_cols; j++)
        {
            SM(i,j) = 1-cos_sim(A.col(i),B.col(j));
        }
    }
    return SM;
}


double cos_sim(vec A, vec B)
{
    /* cos_sim
      This function calculates cosine similarity between vector A and B.
        ~input~
          just vectors.
        ~output~
          obviously, cosine similarity.
      Yoonchang Han 2015/06/26
    */
    return dot(A,B)/(Vmag(A)*Vmag(B));
}


double Vmag(vec A)
{
    /* Vmag
      This function calculates magnitude of the input vector.
        ~input~
          input vector you want to get magnitude.
        ~output~
          magnitude of the input vector.
      Yoonchang Han 2015/06/26
    */
    vec aa = square(A);
    return sqrt(sum(aa));
}

vec db_grid(int spec_size_2, vec grid_idx, int ie)
{
    /* db_grid
       function calculate time stamp
    */
    vec blip_grid = zeros(spec_size_2,1);

    const int denom = 8;
    vector<double> down_idx;
    down_idx.push_back(0);
    for (int i=ie; i<grid_idx.n_rows; i+=denom) down_idx.push_back(grid_idx(i));

    vec down_idx_vec(down_idx);
    uvec down_idx_vec2 = conv_to<uvec>::from(down_idx_vec.rows(1,down_idx.size()-1));
    for (int j=0; j<down_idx_vec2.n_rows; ++j) blip_grid(down_idx_vec2(j)) = 1;

    return blip_grid;
}

mat bpmgrids(int fs, int hopsize, int len_x)
{
    /* bpmgrids
       bpm template grids generation

       input:
         fs :sampling frequency
         hopsize : hopsize
         len_x : length x

      output:
         bpm_template
    */

    int L = 3;
    double gw_array[3] = {0.8, 1., 0.8};
    vec gw(gw_array,L);

    mat bpm_template = zeros(150,30106);
    vec num_beats = zeros(150,1);
    for (int bpm=60; bpm<=150; ++bpm)
    {
        double beat_interval = 60./bpm*fs;
        num_beats(bpm-1) = ceil(len_x/beat_interval);

        int maxpoint = round(beat_interval*(num_beats(bpm-1)+99) / (float)hopsize) + (L-1);
        vec bpmgrid = zeros(maxpoint+1,1);

        for(int i=0; i<num_beats(bpm-1)+100; ++i)
        {
            for(int j=0; j<L; ++j)
            {
                bpmgrid(round(beat_interval*i/(float)hopsize)+j) = gw(j);
            }
        }

        bpm_template(bpm-1,span(0,bpmgrid.n_rows-1)) = bpmgrid.t();
//        beat_hop(bpm-1) = ceil(beat_interval/(float)hopsize);
    }

    return bpm_template;
}

