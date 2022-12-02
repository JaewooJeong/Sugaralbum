#include "../include/MASP.h"
#define M_NUMCH 40
#include <typeinfo>

mat wav2chroma_iy(vec x, int fs) {
    /* WAV2CHROMA_IY
            convert PCM audio data (x) to chromagram matrix
            it uses pre-calculated kernal matrices (F2CQ, CQ2Chroma),
            thus NFFT is fixed to 4096, HOPSIZE is also fixed to 2048
            has 1 bin to each pitches

        MATLAB function written by Il-Young
        JaehunKim 2015/05/28
    */

    const int N = 4096;
    cx_mat Y = stft(x, N, hamming(N), N/2., fs);
    mat X = abs(Y);
    mat F2CQ, CQ2Chroma;
    F2CQ.load("data/F2CQ.dat",csv_ascii);
    CQ2Chroma.load("data/CQ2Chroma.dat", csv_ascii);

    X = F2CQ*X;
    X = X/(repmat(mean(X,1),1,X.n_cols)+datum::eps);
    X = CQ2Chroma*X;

    return X;
}

// void audioread(string fn, vec& x, int* fs) {
//     /* Function to read audio
//         TODO :
//             1. clear input / output type more elegantly
//             2. expand to get any input channel shape (for multichannel audio)

//         JaehunKim 2015/05/28
//     */

//     // init and open file
//     SndfileHandle sndfile = SndfileHandle(fn.c_str());

//     int f = sndfile.frames();
//     int c = sndfile.channels();
//     int sr = sndfile.samplerate();
//     int num_items = f * c;

//     // load data
//     double *buf = new double[num_items]();
//     sndfile.read(buf,num_items);

//     // assign outputs
//     *fs = sr;
//     x = vec(buf, num_items);

//     // free double buffer
//     delete [] buf;
// }

// void audioread_short(string fn, short** x, int* fs, int* lenx) {
//     /* Function to read audio
//             function for memory optimization test

//         JaehunKim 2015/08/18
//     */

//     // init and open file
//     SndfileHandle sndfile = SndfileHandle(fn.c_str());

//     int f = sndfile.frames();
//     int c = sndfile.channels();
//     int sr = sndfile.samplerate();
//     int num_items = f * c;

//     // load data
//     short *buf = new short[num_items];
//     sndfile.read(buf,num_items);

//     // assign outputs
//     *fs = sr; // assign sampling rate

//     // assign PCM data
//     short* tmp = (short*)realloc(*x, sizeof(short)*num_items);
//     memcpy(tmp,buf,sizeof(short)*num_items);
//     *x = tmp;
//     *lenx = num_items/c;

//     // free double buffer
//     delete [] buf;
// }

vec short2vec(short* x, int sz) {

    vec y = zeros(sz,1);
    for (int i=0; i<sz; ++i) {
        float f = ((float) x[i]) / (float) 32768;
        if (f>1) f = 1.;
        if (f<-1) f = -1.;
        y(i) = f;
    }

    return y;
}


/* ---------- Mel Spec related Private Functions ---------------- */

double melScaleInverse(double f) {
    /* Mel Scale Calculation (Inverse)
            Compute Mel Scale (inverse)

        JaehunKim 2015/08/04
    */

    return 700. * (pow(10, f / 2595.0) - 1.);
}

double melScale(double f) {
    /* Mel Scale Calculation
            Compute Mel Scale

        JaehunKim 2015/08/04
    */

    return 2595. * log10(1 + (f / 700.0));
}

vec buildFilterBank(int fs) {
    /* Build Mel Filter Bank
            compute mel-filter bank bound frequencies

        JaehunKim 2015/08/04
    */

    int i = 0;
    vec filterBank = zeros(M_NUMCH,1);
    double melDelta = (melScale(fs/2.) - melScale(0.)) / (float)M_NUMCH;
    for(i = 0; i < M_NUMCH; i++) {
        filterBank(i) = melScaleInverse(melDelta * i);
    }
    return filterBank;
}

vec gen_mel_kern(int nfft, int fs) {
    /* Generate Mel Scale Kernel Matrix Index
            Just same as following function.
            only difference is that it generates only the
            bound index on spectrum bin

        JaehunKim 2015/08/04
    */
    vec fi = zeros(nfft,1);
    for (int i=0; i<nfft; ++i) fi(i) = (fs/(float)nfft) * i;
    vec fi_half = fi.rows(0,nfft/2);
    vec fb = buildFilterBank(fs);

    vec fi2mel = zeros(nfft/2+1,1);
    double j=1;
    for (int i=0; i<nfft/2+1; ++i) {
        if (j<M_NUMCH) {
            if (fi_half(i) > fb(j)) j+=1.;
            fi2mel(i) = j;
        } else {
            fi2mel(i) = j;
        }
    }
    return fi2mel;
}

cx_mat gen_mel_mat(int nfft, int fs) {
    /* Generate Mel Scale Kernel Matrix
            compute kernel matrix for compute mel-spectrogram
            this version does not generate ordinary filterbank
            it only generates rectangular (uni-weighted) filter banks without overlapping,
            which means just sum sequential areas to one bin

        JaehunKim 2015/08/04
    */
    vec fi = zeros(nfft,1);
    for (int i=0; i<nfft; ++i) fi(i) = (fs/(float)nfft) * i;
    vec fi_half = fi.rows(0,nfft/2);
    vec fb = buildFilterBank(fs);

    cx_mat kermat = zeros<cx_mat>(M_NUMCH,nfft/2+1);
    double j=1;
    for (int i=0; i<nfft/2+1; ++i) {
        if (j<M_NUMCH) {
            if (fi_half(i) > fb(j)) j+=1.;
            kermat(j-1,i) = cx_double(1.,1.);
        } else {
            kermat(j-1,i) = cx_double(1.,1.);
        }
    }
    return kermat;
}

/* ---------- Mel Spec related Private Functions ---------------- */


cx_mat melspec(vec x, int nfft, vec window, int noverlap, int fs) {
    /* MelSpectrogram
            compute stft from input time domain signal x
            based on matlab function written by Taesu Kim (2003)
            and transform it to mel-specgtrogram

        JaehunKim 2015/08/04

    */
    int wlen = window.n_rows;
    int shift = wlen - noverlap;

    int n = x.n_rows;
    vec _x = x; //join_cols(join_cols(zeros(wlen,1),x),zeros(nfft,1));  // zero-padding removed (ychan 30/06/15)
    int l = floor((double)(n-noverlap)/(double)shift);  // changed due to removed zero-padding (ychan 30/06/15)
    cx_mat melmat = gen_mel_mat(nfft,fs);

    cx_mat Y = zeros<cx_mat>(M_NUMCH,l);
    vec xn = zeros(nfft,1);

    int sp=0;
    for (int i=0; i<l; ++i) {
        sp = shift*i;
        xn.rows(0,wlen-1) = window % _x.rows(sp,sp+wlen-1);
        cx_vec X = fft_w2(xn,nfft); // 3.3 sec
        Y.col(i) = melmat * X.rows(0,nfft/2);
    }

    return Y;
}

cx_mat melspec_short(short* x, int lenx, int nfft, vec window, int noverlap, int fs) {
    /* MelSpectrogram for Short array data type
            based on above function.
            the purpose of this function is just on memory optimization.

        JaehunKim 2015/08/18

    */
    int wlen = window.n_rows;
    int shift = wlen - noverlap;

    int n = lenx;
    int l = floor((double)(n-noverlap)/(double)shift);  // changed due to removed zero-padding (ychan 30/06/15)
    cx_mat melmat = gen_mel_mat(nfft,fs);

    cx_mat Y = zeros<cx_mat>(M_NUMCH,l);
    vec xn = zeros(nfft,1);
    vec _x = zeros(nfft,1);

    int sp=0;
    for (int i=0; i<l; ++i) {
        sp = shift*i;

        for (int j=0; j<wlen; ++j) {
            float f = ((float) x[sp+j]) / (float) 32768;
            if (f>1) f = 1.;
            if (f<-1) f = -1.;
            _x(j) = f;
        }

        xn.rows(0,wlen-1) = window % _x;
        cx_vec X = fft_w2(xn,nfft); // 3.3 sec
        Y.col(i) = melmat * X.rows(0,nfft/2);
    }

    return Y;
}

cx_mat stft(vec x, int nfft, vec window, int noverlap, int fs) {
    /* Short-time Fourier Transform Analysis
            compute stft from input time domain signal x
            based on matlab function written by Taesu Kim (2003)

        JaehunKim 2015/05/28
    */
    int wlen = window.n_rows;
    int shift = wlen - noverlap;

    int n = x.n_rows;
    vec _x = x; //join_cols(join_cols(zeros(wlen,1),x),zeros(nfft,1));  // zero-padding removed (ychan 30/06/15)
    int l = floor((double)(n-noverlap)/(double)shift);  // changed due to removed zero-padding (ychan 30/06/15)
    cx_mat Y = zeros<cx_mat>(nfft,l);
    vec xn = zeros(nfft,1);

    int sp=0;
    for (int i=0; i<l; i++) {
        sp = shift*i;
        xn.rows(0,wlen-1) = window % _x.rows(sp,sp+wlen-1);
//        Y.col(i) = fft(xn); // 20 sec
//        Y.col(i) = fft_jh(xn,nfft); // 8.5 sec
        Y.col(i) = fft_w2(xn,nfft); // 3.3 sec
    }

    return Y.rows(0,floor(nfft/2));
}

//cx_mat stft_half(vec x, int nfft, vec window, int noverlap, int fs) {
//    /* Short-time Fourier Transform Analysis
//            compute stft from input time domain signal x
//            based on matlab function written by Taesu Kim (2003)
//
//        JaehunKim 2015/05/28
//    */
//    int wlen = window.n_rows;
//    int shift = wlen - noverlap;
//
//    int n = x.n_rows;
//    vec _x = x; //join_cols(join_cols(zeros(wlen,1),x),zeros(nfft,1));  // zero-padding removed (ychan 30/06/15)
//    int l = floor((double)(n-noverlap)/(double)shift);  // changed due to removed zero-padding (ychan 30/06/15)
//    cx_mat Y = zeros<cx_mat>(floor(nfft/2)+1,l);
//    vec xn = zeros(nfft,1);
//
//    int sp=0;
//    for (int i=0; i<l; i++) {
//        sp = shift*i;
//        xn.rows(0,wlen-1) = window % _x.rows(sp,sp+wlen-1);
//        Y.col(i) = fft_w(xn,nfft); // 3.3 sec
//    }
//
//    return Y;
//}

vec istft(cx_mat X, vec window, int noverlap) {
    /* Short-time Fourier Transform Analysis
            compute stft from input time domain signal x
            based on matlab function written by Taesu Kim (2003)

        JaehunKim 2015/05/28
    */
    int nfft = (X.n_rows-1) * 2;
    int l = X.n_cols;

    int wlen = window.n_rows;
    int shift = wlen - noverlap;

    vec w = zeros(X.n_rows + wlen + nfft, 1);

    cx_vec X_even(X.n_rows-2,X.n_cols);
    int k=0;
    for (int j=X.n_rows-2; j>0; j--) {
        X_even.col(k) = X.col(j);
        k++;
    }
    cx_mat XN = join_vert(X,conj(X_even));

    vec y = zeros(X.n_rows+wlen + nfft, 1);

    int sp = 0;
    vec tmp;
    for (int i=0; i<l; i++) {
        sp = shift*i ;
        tmp = real(ifft_jh(X.col(i),nfft));

        w.rows(sp, sp+wlen-1) = w.rows(sp, sp+wlen-1) + pow(window,2);
        y.rows(sp, sp+wlen-1) = y.rows(sp,sp+wlen-1) + window % tmp.rows(0,wlen-1);
    }

    w.rows(shift*(l+1), shift*(l+1)+wlen) = w.rows(shift*(l+1), shift*(l+1)+wlen) + pow(window,2);

    y = y.rows(wlen,wlen+X.n_rows) / w.rows(wlen,wlen+X.n_rows);

    return y;
}

vec hamming(int len) {
    /* Hamming window function
            compute Hamming window length 'len'

        JaehunKim 2015/05/28
    */
    return 0.54 - 0.46 * cos((2*M_PI*linspace<vec>(0,len-1,len))/(len-1));
}

cx_vec fft_jh(const vec x, int nfft) {
    /* FFT function using kiss fft wrapper contained in ConstantQ lib

        TODO:
            1. use native kiss fft for optimizing speed

        JaehunKim 2015/05/28
    */

    // initialize fft handler (kissfft) and other variables
    FFTReal fft_handler(nfft);
    double* ro = new double[nfft](); // real ouput
    double* io = new double[nfft](); // imag output

    // compute fft and assign to output vector
    fft_handler.forward(x.memptr(),ro,io);
    cx_vec X(vec(ro,nfft),vec(io,nfft));

    delete[] ro;
    delete[] io;

    return X;
}

vec ifft_jh(const cx_vec x, int nfft) {
    /* FFT function using kiss fft wrapper contained in ConstantQ lib

        TODO:
            1. use native kiss fft for optimizing speed

        JaehunKim 2015/05/28
    */

    // initialize fft handler (kissfft) and other variables
    FFTReal fft_handler(nfft);
    vec ri = real(x);
    vec ii = imag(x);
    vec ro(nfft,1);

    // compute fft and assign to output vector
    fft_handler.inverse(ri.memptr(), ii.memptr(),ro.memptr());

    return ro;
}

//cx_vec fft_w3(vec x, int nfft) {
//    /* FFT function using fftw3
//
//        JaehunKim 2015/05/28
//    */
//    vec x_padded;
//    if (x.n_rows < nfft) {
//        x_padded = zeros(nfft,1);
//        x_padded.rows(0,x.n_rows-1) = x % hamming(x.n_rows);
//    }
//
//    cx_vec X(nfft/2+1,1);
//    fftw_complex* X_w = reinterpret_cast<fftw_complex*> (X.colptr(0));
//    fftw_plan plan;
//    plan = fftw_plan_dft_r2c_1d(nfft, x.colptr(0), X_w, FFTW_ESTIMATE);
//    fftw_execute(plan);
//    fftw_destroy_plan(plan);
//
//    return X;
//}


cx_vec fft_w2(vec x, int nfft) {
    /* FFT function using fftw2

        JaehunKim 2015/05/28
    */

    vec x_pd;
    if (x.n_rows < nfft) {
        x_pd = zeros(nfft,1);
        x_pd.rows(0,x.n_rows-1) = x % hamming(x.n_rows);
    } else {
        x_pd = x;
    }

    // initialize containers
    cx_vec X(nfft,1);
//    fftw_complex *x_w, *X_w;
//    x_w = (fftw_complex*) fftw_malloc(sizeof(fftw_complex)*nfft);
//    X_w = (fftw_complex*) fftw_malloc(sizeof(fftw_complex)*nfft);
    fftw_complex x_w[nfft], X_w[nfft];


    for (int i=0; i<nfft; ++i) {
        x_w[i].re = x_pd(i);
        x_w[i].im = 0.;
    }

    // plan initialization
    fftw_plan plan = fftw_create_plan(nfft,FFTW_FORWARD,FFTW_ESTIMATE);

    // execute transform
    fftw_one(plan,x_w,X_w);

    // Assign result to cx_vec
    for (int i=0; i<nfft; ++i) X(i) = cx_double(X_w[i].re, X_w[i].im);

    // destroy plan
    fftw_destroy_plan(plan);
    //fftw_free(x_w); fftw_free(X_w);

    return X;
}

void imshow(mat X, string fn) {
    /* IMSHOW simple function works silmilar to matlab imshow
            using python script by c++ system function
            as limit of current structure which using armadillo '.save()' method,
            filename (fn) should be fed to this function

        JaehunKim 2015/06/01
    */

    // save result to csv
    X.save(fn, csv_ascii);

    // visualize output
    string py_cmd = "python data/visualize.py " + fn;
    system(py_cmd.c_str());
}

void plot(vec x, string fn) {
    /* PLOT simple function works silmilar to matlab plot
            using python script by c++ system function
            as limit of current structure which using armadillo '.save()' method,
            filename (fn) should be fed to this function

        JaehunKim 2015/06/01
    */

    // save result to csv
    x.save(fn, csv_ascii);

    // visualize output
    string py_cmd = "python data/visualize.py " + fn;
    system(py_cmd.c_str());
}

vec flux(mat X) {
    /* FLUX

        JaehunKim 2015/06/10
    */
    vec y(X.n_cols,1);

    for (int i=0; i<X.n_cols-1; ++i) {
        vec tmp = normalise(X.col(i+1)) - normalise(X.col(i));
        y.row(i+1) = norm(tmp,2);
    }

    return y;
}

vec simplePeakFind(double environment, vec data, double thresh) {
    /* SIMPLEPEAKFIND
            finds the positions of peaks in a
            given data list. valid peaks
            are searched to be greater than the threshold value.
            peaks are searched to be maximum in a certain environment
            of values in the list
                -based on cresspahl's blog

        JaehunKim 2015/06/17
    */

    double listlength = data.n_rows;
    vec peaklist = zeros(listlength,1); // create blank output
    double searchEnvHalf = max(1.,floor(environment/2));
    //
    // we only have to consider date above the threshold
    //
    uvec dataAboveThreshIndx = find(data >= thresh);
    for (int candidateIndx=0; candidateIndx<dataAboveThreshIndx.n_rows;++candidateIndx) {
        int Indx = dataAboveThreshIndx(candidateIndx);
        //
        //  consider list boundaries
        //
        int minindx = Indx - searchEnvHalf;
        int maxindx = Indx + searchEnvHalf;
        if (minindx < 0) minindx = 0;
        if (maxindx >= listlength) maxindx = listlength-1;

        if (data(Indx) == max(data.rows(minindx,maxindx))) peaklist(Indx) = Indx;
    }

    peaklist = peaklist(find(peaklist));

    return peaklist;
}


// void process_mem_usage(double& vm_usage, double& resident_set) {
//     //////////////////////////////////////////////////////////////////////////////
//     // http://stackoverflow.com/questions/669438/how-to-get-memory-usage-at-run-time-in-c
//     //
//     // process_mem_usage(double &, double &) - takes two doubles by reference,
//     // attempts to read the system-dependent data for a process' virtual memory
//     // size and resident set size, and return the results in KB.
//     //
//     // On failure, returns 0.0, 0.0
//     //
//     // from Don Wakefield

//     using std::ios_base;
//     using std::ifstream;
//     using std::string;

//     vm_usage     = 0.0;
//     resident_set = 0.0;

//     // 'file' stat seems to give the most reliable results
//     //
//     ifstream stat_stream("/proc/self/stat",ios_base::in);

//     // dummy vars for leading entries in stat that we don't care about
//     //
//     string pid, comm, state, ppid, pgrp, session, tty_nr;
//     string tpgid, flags, minflt, cminflt, majflt, cmajflt;
//     string utime, stime, cutime, cstime, priority, nice;
//     string O, itrealvalue, starttime;

//     // the two fields we want
//     //
//     unsigned long vsize;
//     long rss;

//     stat_stream >> pid >> comm >> state >> ppid >> pgrp >> session >> tty_nr
//                >> tpgid >> flags >> minflt >> cminflt >> majflt >> cmajflt
//                >> utime >> stime >> cutime >> cstime >> priority >> nice
//                >> O >> itrealvalue >> starttime >> vsize >> rss; // don't care about the rest

//     stat_stream.close();

//     long page_size_kb = sysconf(_SC_PAGE_SIZE) / 1024; // in case x86-64 is configured to use 2MB pages
//     vm_usage     = vsize / 1024.0;
//     resident_set = rss * page_size_kb;
// }
