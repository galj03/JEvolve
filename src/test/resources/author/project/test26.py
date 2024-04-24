import numpy as np

def function1(sentence,callbacks):
    ff = {"one":1,"two":2}
    print(ff)
    z=0
    for v in ff.values():
        z=z+v
    return z