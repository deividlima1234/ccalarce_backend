
import requests
import json

base_url = "https://ccalarce-backend.onrender.com/api/v1"
auth_url = f"{base_url}/auth/authenticate"
history_url = f"{base_url}/liquidation/history?page=0&size=10"

creds = {
    "username": "admin",
    "password": "deivid123"
}

try:
    print(f"Authenticating to {auth_url}...")
    resp = requests.post(auth_url, json=creds)
    print(f"Auth Status: {resp.status_code}")
    
    if resp.status_code != 200:
        print("Auth failed:", resp.text)
        exit(1)
        
    token = resp.json().get("token")
    print("Token received.")
    
    # Decode JWT payload
    try:
        import base64
        payload_part = token.split('.')[1]
        # Add padding if needed
        payload_part += '=' * (-len(payload_part) % 4)
        payload = json.loads(base64.b64decode(payload_part).decode('utf-8'))
        print("JWT Payload:", json.dumps(payload, indent=2))
    except Exception as e:
        print("Failed to decode JWT:", e)

    headers = {
        "Authorization": f"Bearer {token}"
    }
    
    print(f"Requesting history from {history_url}...")
    hist_resp = requests.get(history_url, headers=headers)
    print(f"History Status: {hist_resp.status_code}")
    
    nonsense_url = f"{base_url}/liquidation/nonsense"
    print(f"Requesting nonsense from {nonsense_url}...")
    non_resp = requests.get(nonsense_url, headers=headers)
    print(f"Nonsense Status: {non_resp.status_code}")
    
    debug_url = f"{base_url}/liquidation/debug-info"
    print(f"Requesting debug info from {debug_url}...")
    debug_resp = requests.get(debug_url, headers=headers)
    print(f"Debug Status: {debug_resp.status_code}")
    print("Debug Body:", debug_resp.text)
    
    print("Response Body Head:")
    print(hist_resp.text[:500])
    
except Exception as e:
    print(f"Error: {e}")
