(function(d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s); js.id = id;
    js.src = "//connect.facebook.net/en_US/sdk.js#xfbml=1&version=v2.10&appId=133674077141287";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));
function onFacebookLoginButtonClick() {
    document.getElementById("facebookErrorMessage").style.visibility = "collapse";
    FB.getLoginStatus(function(response) {
        if (response.status === 'connected') {
            post('/facebook_auth', {
                accessToken: response.authResponse.accessToken,
                expiresIn: response.authResponse.expiresIn,
                signedRequest: response.authResponse.signedRequest,
                userID: response.authResponse.userID
            });
        } else {
            document.getElementById("facebookErrorMessage").style.visibility = "visible";
        }
    });
}