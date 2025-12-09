using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using ToursManager.Api.Data;
using ToursManager.Api.Services;

var builder = WebApplication.CreateBuilder(args);

// ===== 1. Configure Database (Entity Framework Core with PostgreSQL) =====
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? throw new InvalidOperationException("Connection string 'DefaultConnection' not found.");

builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseNpgsql(connectionString));

// ===== 2. Configure JWT Authentication =====
var jwtSecret = builder.Configuration["Jwt:Secret"]
    ?? throw new InvalidOperationException("JWT secret not configured");

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtSecret)),
        ValidateIssuer = true,
        ValidIssuer = builder.Configuration["Jwt:Issuer"],
        ValidateAudience = true,
        ValidAudience = builder.Configuration["Jwt:Audience"],
        ValidateLifetime = true,
        ClockSkew = TimeSpan.Zero  // No tolerance for token expiration
    };
});

builder.Services.AddAuthorization();

// ===== 3. Configure CORS =====
var allowedOrigins = builder.Configuration["Cors:AllowedOrigins"]
    ?.Split(',', StringSplitOptions.RemoveEmptyEntries)
    ?? new[] { "http://localhost:5173" };

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowFrontend", policy =>
    {
        policy.WithOrigins(allowedOrigins)
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials();
    });
});

// ===== 4. Register Services (Dependency Injection) =====
builder.Services.AddScoped<GuideService>();
builder.Services.AddScoped<JwtService>();

// ===== 5. Add Controllers =====
builder.Services.AddControllers();

// ===== 6. Configure Swagger/OpenAPI =====
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "Tours Manager API",
        Version = "v1",
        Description = "REST API for Tours Manager application - .NET version"
    });
    
    // Add JWT Authentication to Swagger
    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Name = "Authorization",
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        In = ParameterLocation.Header,
        Description = "Enter 'Bearer' [space] and then your JWT token"
    });
    
    c.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });
});

// ===== Build the application =====
var app = builder.Build();

// ===== 7. Configure HTTP Request Pipeline (Middleware) =====

// Swagger (Development only)
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "Tours Manager API v1");
        c.RoutePrefix = "swagger";  // Access at http://localhost:5000/swagger
    });
}

// HTTPS Redirection (comment out for dev if needed)
app.UseHttpsRedirection();

// CORS - MUST come before Authentication
app.UseCors("AllowFrontend");

// Authentication & Authorization
app.UseAuthentication();  // Read JWT token from request
app.UseAuthorization();   // Check if user is authorized

// Map Controllers
app.MapControllers();

// Root endpoint
app.MapGet("/", () => Results.Ok(new
{
    message = "Tours Manager API - .NET Version",
    version = "1.0.0",
    endpoints = new
    {
        swagger = "/swagger",
        health = "/api/health",
        auth = new
        {
            register = "POST /api/auth/register",
            login = "POST /api/auth/login"
        },
        guides = new
        {
            getAll = "GET /api/guides",
            getById = "GET /api/guides/{id}",
            getProfile = "GET /api/guides/profile (requires JWT)",
            updateProfile = "PATCH /api/guides/profile (requires JWT)",
            replaceProfile = "PUT /api/guides/profile (requires JWT)"
        }
    }
}))
.WithName("Root")
.WithTags("Info");

// Health check endpoint
app.MapGet("/api/health", () => Results.Ok(new
{
    status = "healthy",
    timestamp = DateTime.UtcNow,
    environment = app.Environment.EnvironmentName
}))
.WithName("HealthCheck")
.WithTags("Info");

app.Run();
