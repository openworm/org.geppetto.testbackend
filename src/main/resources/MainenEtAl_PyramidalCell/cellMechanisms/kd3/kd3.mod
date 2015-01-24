
COMMENT
            
            Kd3h5.mod
            
            Potassium channel, Hodgkin-Huxley style kinetics
            Kinetic rates based roughly on Sah et al. and Hamill et al. (1991)
            
            Use with na3h5.mod
            
            Author: Zach Mainen, Salk Institute, 1994, zach@salk.edu
            	
ENDCOMMENT


INDEPENDENT {t FROM 0 TO 1 WITH 1 (ms)}
 
NEURON {
    SUFFIX %Name%
    USEION k READ ek WRITE ik
    RANGE n, gk, gmax, ninf, ntau
    GLOBAL Ra, Rb, q10, temp, tadj, vmin, vmax
}

UNITS {
    (mA) = (milliamp)
    (mV) = (millivolt)
    (pS) = (picosiemens)
    (um) = (micron)
}

PARAMETER {
    gmax = %Max Conductance Density% (pS/um2)
    v (mV)
    
    tha = 20.0 (mV) : v 1/2 for inf
    qa = 9.0 (mV) : inf slope
    
    Ra = 0.02 (/ms) : max act rate
    Rb = 0.0020 (/ms) : max deact rate
    
    dt (ms)
    celsius (degC)
    temp = 16.0 (degC) : original temp
    q10 = 2.3 : temperature sensitivity
    
    vmin = -120.0 (mV)
    vmax = 100.0 (mV)
}

ASSIGNED {
    a (/ms)
    b (/ms)
    ik (mA/cm2)
    gk (pS/um2)
    ek (mV)
    ninf
    ntau (ms)
    tadj
}

STATE { n }

INITIAL {
            	trates(v)
            	n = ninf
}

BREAKPOINT {
                    SOLVE states
            	gk = gmax*n
            	ik = (1e-4) * gk * (v - ek)
}

LOCAL nexp
 
PROCEDURE states() {
                    trates(v)      :             at the current v and dt.
                    n = n + nexp*(ninf-n)
                    VERBATIM
                    return 0;
                    ENDVERBATIM
}

PROCEDURE trates(v) {
                                  :Call once from HOC to initialize inf at resting v.
                    LOCAL tinc
                    TABLE ninf, nexp
            	DEPEND dt, celsius, temp, Ra, Rb, tha, qa
            	
            	FROM vmin TO vmax WITH 199
            
            	rates(v): not consistently executed from here if usetable_hh == 1
            
                    tadj = 3^((celsius - temp)/10)
            
                    tinc = -dt * tadj
                    nexp = 1 - exp(tinc/ntau)
}

PROCEDURE rates(v) {
                                  :Call once from HOC to initialize inf at resting v.
            
                    a = Ra * (v - tha) / (1 - exp(-(v - tha)/qa))
                    b = -Rb * (v - tha) / (1 - exp((v - tha)/qa))
                    ntau = 1/(a+b)
            	ninf = a*ntau
                
                
                    
    VERBATIM
        //printf("Real v: %f, alpha: %f, beta: %f, tau: %f\n", _lv, a, b, ntau);
        //printf("A: %f, k: %f, d: %f ...\n", _lA, _lk, _ld);
    ENDVERBATIM  
}

