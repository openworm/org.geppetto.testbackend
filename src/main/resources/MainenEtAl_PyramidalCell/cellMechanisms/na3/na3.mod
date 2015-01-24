
COMMENT
            
            na3h5.mod
            
            Sodium channel, Hodgkin-Huxley style kinetics.  
            
            Kinetics were fit to data from Huguenard et al. (1988) and Hamill et
            al. (1991)
            
            qi is not well constrained by the data, since there are no points
            between -80 and -55.  So this was fixed at 5 while the thi1,thi2,Rg,Rd
            were optimized using a simplex least square proc
            
            voltage dependencies are shifted approximately +5mV from the best
            fit to give higher threshold
            
            use with kd3h5.mod
            
            Author: Zach Mainen, Salk Institute, 1994, zach@salk.edu
            
ENDCOMMENT


INDEPENDENT {t FROM 0 TO 1 WITH 1 (ms)}
 
NEURON {
    SUFFIX %Name%
    USEION na READ ena WRITE ina
    RANGE m, h, gna, gmax, vshift, minf, hinf, mtau, htau
    GLOBAL tha, thi1, thi2, qa, qi, qinf, thinf, Ra, Rb, Rd, Rg, q10, temp, tadj, vmin, vmax
}

PARAMETER {
    gmax = %Max Conductance Density% (pS/um2) 
    
    vshift = 0.0 (mV) : voltage shift (affects all)
    
    tha = -35.0 (mV) : v 1/2 for act		(-42)
    qa = 9.0 (mV) : act slope
    Ra = 0.182 (/ms) : open (v)
    Rb = 0.124 (/ms) : close (v)
    
    thi1 = -50.0 (mV) : v 1/2 for inact
    thi2 = -75.0 (mV) : v 1/2 for inact
    qi = 5.0 (mV) : inact tau slope
    thinf = -65.0 (mV) : inact inf slope
    qinf = 6.2 (mV) : inact inf slope
    Rg = 0.0091 (/ms) : inact (v)
    Rd = 0.024 (/ms) : inact recov (v)
    
    temp = 23.0 (degC) : original temp
    q10 = 2.3 : temperature sensitivity
    
    v (mV)
    dt (ms)
    celsius (degC)
    vmin = -120.0 (mV)
    vmax = 100.0 (mV)
}

UNITS {
    (mA) = (milliamp)
    (mV) = (millivolt)
    (pS) = (picosiemens)
    (um) = (micron)
}

ASSIGNED {
    ina (mA/cm2)
    gna (pS/um2)
    ena (mV)
    minf
    hinf
    mtau (ms)
    htau (ms)
    tadj
}

STATE {
    m
    h
}

INITIAL {
            	trates(v+vshift)
            	m = minf
            	h = hinf
}

BREAKPOINT {
                    SOLVE states
                    gna = gmax*m*m*m*h
            	ina = (1e-4) * gna * (v - ena)
}

LOCAL mexp, hexp
 
PROCEDURE states() {
                    trates(v+vshift)      :             at the current v and dt.
                    m = m + mexp*(minf-m)
                    h = h + hexp*(hinf-h)
                    VERBATIM
                    return 0;
                    ENDVERBATIM
}

PROCEDURE trates(v) {
                                  
                    LOCAL tinc
                    TABLE minf, mexp, hinf, hexp
            	DEPEND dt, celsius, temp, Ra, Rb, Rd, Rg, tha, thi1, thi2, qa, qi, qinf
            	
            	FROM vmin TO vmax WITH 199
            
            	rates(v): not consistently executed from here if usetable == 1
            
                    tadj = q10^((celsius - temp)/10)
                    tinc = -dt * tadj
            
                    mexp = 1 - exp(tinc/mtau)
                    hexp = 1 - exp(tinc/htau)
}

PROCEDURE rates(vm) {
                    LOCAL  a, b
            
            	a = trap0(vm,tha,Ra,qa)
            	b = trap0(-vm,-tha,Rb,qa)
            	mtau = 1/(a+b)
            	minf = a*mtau
                    
                                    
    VERBATIM
        //printf("Real m: %f, a: %f, b: %f, tau: %f, inf: %f\n", _lvm, _la, _lb, mtau, minf);
    ENDVERBATIM          
            		:"h" inactivation 
            
            	a = trap0(vm,thi1,Rd,qi)
            	b = trap0(-vm,-thi2,Rg,qi)
            	htau = 1/(a+b)
            	hinf = 1/(1+exp((vm-thinf)/qinf))
                
                
                                    
    VERBATIM
        //printf("Real h: %f, a: %f, b: %f, tau: %f, inf: %f\n", _lvm, _la, _lb, htau, hinf);
    ENDVERBATIM  
}

FUNCTION trap0(v,th,a,q) {
            	if (fabs(v/th) > 1e-6) {
            	        trap0 = a * (v - th) / (1 - exp(-(v - th)/q))
            	} else {
            	        trap0 = a * q
             	}
}

