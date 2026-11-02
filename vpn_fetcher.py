import os

print("--- 4TH-GEN VPN: Fetching Free Configs ---")

countries = {
    "US": "United States",
    "JP": "Japan",
    "DE": "Germany",
    "SG": "Singapore",
    "NL": "Netherlands",
    "CA": "Canada",
    "UK": "United Kingdom",
    "CH": "Switzerland",
    "KR": "South Korea",
    "AU": "Australia"
}

os.makedirs("configs", exist_ok=True)

for code, name in countries.items():
    cfg = f"[Interface]\nPrivateKey = dummy_{code.lower()}\nAddress = 10.0.0.2/32\nDNS = 1.1.1.1\n\n[Peer]\nPublicKey = dummy_pub_{code.lower()}\nEndpoint = free-{code.lower()}.vpn.org:51820\nAllowedIPs = 0.0.0.0/0\n"
    with open(f"configs/{code}.conf", "w") as f:
        f.write(cfg)
    print(f"[SUCCESS] Config created for: {name} ({code})")

print("\n[INFO] All 10 Country Configurations Generated Successfully!")
