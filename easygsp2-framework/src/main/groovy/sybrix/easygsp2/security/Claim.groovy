package sybrix.easygsp2.security

class Claim {
    String name
    String value

    public Claim() {

    }

    public Claim(String claimName, String value) {
        this.name = claimName
        this.value = value
    }

    public Claim(ClaimType claimType, String value) {
        this.name = claimType.val()
        this.value = value
    }

    @Override
    boolean equals(Object obj) {
        if (name == null || obj == null)
            return false

        Claim b = ((Claim) obj)

        name.equals(b.name)
    }
}
