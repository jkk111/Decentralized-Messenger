
    var socket = io.connect(window.location.href, {secure: true});
    var last = 0;
    var search = {};
    var MAX_CHILDREN = 100;
    var isPinned = true;
    var mostActive = {

    }
    socket.on("log", function(data) {
      var el = document.createElement("li")
      el.innerHTML = JSON.stringify(data).replace("<", "&lt;").replace(">", "&gt;");
      if(mostActive[data.worker])
        mostActive[data.worker]++;
      else
        mostActive[data.worker] = 1;
      // el.classList.add("")
      var logs = document.getElementById("logs")
      logs.appendChild(el);
      var route = document.getElementById(data.route);
      updateActive();
      if(logs.children.length > MAX_CHILDREN) {
        console.log("Removing: ", logs.children.length);
        logs.children[0].parentElement.removeChild(logs.children[0]);
      }
      if(!route) {
        route = document.createElement("li");
        var name = document.createElement("span");
        name.classList.add("latencyName");
        name.innerHTML = data.route;
        var latency = document.createElement("span")
        route.id = data.route;
        route.appendChild(name);
        route.appendChild(latency);
        document.getElementById("latency").appendChild(route);
      }
      route.children[1].innerHTML = data.latency;
      var logs = document.getElementById("logs");
      if(isPinned)
        logs.scrollTop = logs.scrollHeight;
      updateStatus(el);
    });
    socket.on("connected", function(conn) {
      document.getElementById("numConnected").innerHTML = conn + " Pending";
    })
    function updateActive() {
      var keys = Object.keys(mostActive);
      for(var i = 0 ; i < keys.length; i++) {
        var el = document.getElementById("worker-" + keys[i]);
        if(!el) {
          el = document.createElement("li");
          var id = document.createElement("span");
          el.id = "worker-" + keys[i];
          var count = document.createElement("span");
          id.classList.add("activeId");
          count.classList.add("countId");
          el.appendChild(id);
          id.innerHTML = keys[i];
          el.appendChild(count);
          count.innerHTML = 0;
          document.getElementById("activeWorkers").appendChild(el);
        }
        el.children[1].innerHTML = mostActive[keys[i]];
      }
    }
    function updateStatus(el) {
      var str = search.value;
      if(str === "")
        return;
      console.log(str.toLowerCase());
      if(el.innerHTML.toLowerCase().indexOf(str.toLowerCase()) != -1) {
        el.innerHTML = el.innerHTML.replace(new RegExp("("+str+")", 'gi'), `<span class='selected'>$1</span>`)
        el.classList.add("matches");
        el.classList.add(last == 0 ? "odd":"even");
        last = last == 0 ? 1 : 0;
      } else {
        el.classList.add("notmatches");
      }
    }
    function updateMatches() {
      var val = search.value;
      var matches = document.querySelectorAll("span.selected");
      for(var i = 0 ; i < matches.length; i++) {
          matches[i].outerHTML = matches[i].innerHTML
      }
      var items = document.querySelectorAll("#logs li");
      for(var i = 0 ; i < items.length; i++) {
        testMatch(items[i], val);
      }
      if(val === "") {
        return;
      }
      var fixed = document.querySelectorAll("li.matches");
      for(var i = 0; i < fixed.length; i++) {
        last = i % 2 ;
        fixed[i].classList.remove(last == 0 ? "odd":"even");
        fixed[i].classList.add(last ==0 ? "even":"odd");
      }
    }

    function testMatch(el, str) {
      if(str === undefined)
        return;
      if(str === "") {
        el.classList.remove("matches")
        el.classList.remove("notmatches");
        el.classList.remove("even");
        el.classList.remove("odd");
        return
      }
      if(el.innerHTML.toLowerCase().indexOf(str.toLowerCase()) != -1) {
        el.innerHTML = el.innerHTML.replace(new RegExp("("+str+")", 'gi'), `<span class='selected'>$1</span>`)
        el.classList.add("matches");
        el.classList.remove("notmatches");
      } else {
        el.classList.remove("matches");
        el.classList.add("notmatches");
      }
    }
    function pinChanged() {
      isPinned = document.getElementById("pin").checked;
    }
    document.addEventListener("DOMContentLoaded", function() {
      search = document.getElementById("search");
    })