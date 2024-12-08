from flask import Flask, request, jsonify
import requests

app_logo = [
"  _____     _ _   ____       _            _   _             ",
" |  ___|_ _| | | |  _ \  ___| |_ ___  ___| |_(_) ___  _ __  ",
" | |_ / _` | | | | | | |/ _ \ __/ _ \/ __| __| |/ _ \| '_ \ ",
" |  _| (_| | | | | |_| |  __/ ||  __/ (__| |_| | (_) | | | |",
" |_|  \__,_|_|_| |____/ \___|\__\___|\___|\__|_|\___/|_| |_|",
"\n"
]

app = Flask(__name__)

# put your telegram token and user IDs
TELEGRAM_BOT_TOKEN = "123456789:ABCDefGhIJKlMNOpQrSTUVWxYZ1234567890"
TELEGRAM_USER_IDS = ['123456789']

def send_to_telegram(message):
    # create the url for telegram api
    url = f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendMessage"
    # iterate over each telegram user id
    for user_id in TELEGRAM_USER_IDS:
        # create the payload of the telegram message, with the user id and the text to send
        payload = {
            'chat_id': user_id,
            'text': message
        }
        # make a post request with the previous payload to the url
        response = requests.post(url, json=payload)
        # if the message sending failed
        if response.status_code != 200:
            # notify the user that wasn't possible to send a message to the telegram bot
            print(f"Failed to send message to user {user_id}")
        else:
            # otherwise, print a message to notify the user that the message was sent successfully
            print(f"Message sent to user {user_id}")

@app.route('/message', methods=['POST'])
def receive_message():
    # retrieve the data from the request
    data = request.json
    # if the data contains a message
    if 'message' in data:
        # get the message and print it on the screen
        message = data['message']
        print(f"Received message:\n\n{message}\n")
        # try to send the previous message to the telegram bot
        send_to_telegram(message)
        return jsonify({"status": "success", "message": "Message sent to the server"}), 200
    # if no message are available, return an error code
    return jsonify({"status": "failure", "message": "Invalid message data"}), 400

if __name__ == '__main__':
    # print the fall detection application logo
    for line in app_logo:
        print(line)
    # run the web flask server
    app.run(host='0.0.0.0', port=5000)