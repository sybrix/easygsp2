package sybrix.easygsp2.security

class Claims {
    private Map<String, Claim> data = new HashMap<>()

    def add(ClaimType claimType, Object val) {
        data.put(claimType.val(), new Claim(claimType.val(),  val))
    }

    def add(Claim claim) {
        data.put(claim.getName(), claim)
    }

    def add(String claimName, Object val) {
        data.put(claimName, new Claim(claimName, val))
    }

    Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>()
        for (Claim c : data.values()) {
            m.put(c.name, c.value)
        }

        m
    }

    Claim get(String claimName) {
        data.get(claimName)
    }

    boolean contains(Claim claim) {
        data.values().contains(claim)
    }

    boolean contains(String claimName) {
        data.containsKey(claimName)
    }

    void remove(Claim claim) {
        data.remove(claim.name)
    }

    void remove(String claimName) {
        data.remove(claimName)
    }

}
