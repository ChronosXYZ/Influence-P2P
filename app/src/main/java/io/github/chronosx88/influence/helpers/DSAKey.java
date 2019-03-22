package io.github.chronosx88.influence.helpers;

import java.io.Serializable;
import java.math.BigInteger;

public class DSAKey implements Serializable {
    private BigInteger Q;
    private BigInteger P;
    private BigInteger Y;
    private BigInteger G;

    public DSAKey(BigInteger Q, BigInteger P, BigInteger Y, BigInteger G) {
        this.Q = Q;
        this.P = P;
        this.Y = Y;
        this.G = G;
    }

    public BigInteger getY() {
        return Y;
    }

    public BigInteger getG() {
        return G;
    }

    public BigInteger getP() {
        return P;
    }

    public BigInteger getQ() {
        return Q;
    }
}
