from neuron import h

# create pre-and post-synaptic sections
pre = h.Section()
post = h.Section()

for sec in pre,post: 
    sec.insert('hh')

# inject current in the pre-synaptic section
stim = h.IClamp(0.5, sec=pre)
stim.amp = 10.0
stim.delay = 5.0
stim.dur = 5.0

# create a synapse in the pre-synaptic section
syn = h.ExpSyn(0.5, sec=post)

# connect the pre-synaptic section to the 
# synapse object
nc = h.NetCon(pre(0.5)._ref_v, syn, sec=pre)
nc-weight[0] = 2.0

vec={}
for var in 'v_pre', 'v_post', 'i_syn', 't':
    vec[var] = h.Vector()

# record the membrane potentials and 
# synaptic currents
vec['v_pre'].record(pre(0.5)._ref_v)
vec['v_post'].record(post(0.5)._ref_v)
vec['i_syn'].record(syn._ref_i)
vec['t'].record(h._ref_t)

# run the simulation
h.load_file("stdrun.hoc")
h.init()
h.tstop = 20.0
h.run()

# check the values on command line
list(vec['v_pre'])
list(vec['v_post'])
list(vec['i_syn'])
list(vec['t'])