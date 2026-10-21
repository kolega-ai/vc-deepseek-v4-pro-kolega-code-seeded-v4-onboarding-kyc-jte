@import static com.meridianbank.web.template.Tpl.*
@param java.util.Map<String, Object> flash
@if (flash != null)
    <div class="alert alert-${String.valueOf(flash.get("kind"))}">
        <span class="icon">${flashIcon(String.valueOf(flash.get("kind")))}</span>
        <span>${String.valueOf(flash.get("message"))}</span>
    </div>
@endif
